package com.launchmate.website.service;

import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.SetupStage;
import com.launchmate.business.service.BusinessService;
import com.launchmate.shared.audit.AuditEvent;
import com.launchmate.shared.audit.AuditService;
import com.launchmate.shared.exception.ConflictException;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.website.domain.DraftSource;
import com.launchmate.website.domain.InvalidSiteConfigException;
import com.launchmate.website.domain.Publication;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.domain.SiteConfigValidator;
import com.launchmate.website.domain.Website;
import com.launchmate.website.repository.PublicationRepository;
import com.launchmate.website.repository.WebsiteRepository;
import com.launchmate.website.template.StarterSiteFactory;
import com.launchmate.website.template.TemplateRegistry;
import com.launchmate.website.template.WebsiteTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Draft website use cases. Every write validates the full configuration first. */
@Service
public class WebsiteService {

    private final WebsiteRepository websites;
    private final PublicationRepository publications;
    private final SiteConfigValidator validator;
    private final TemplateRegistry templates;
    private final StarterSiteFactory starterFactory;
    private final BusinessService businesses;
    private final AuditService audit;

    public WebsiteService(WebsiteRepository websites, PublicationRepository publications, SiteConfigValidator validator,
                          TemplateRegistry templates, StarterSiteFactory starterFactory, BusinessService businesses,
                          AuditService audit) {
        this.websites = websites;
        this.publications = publications;
        this.validator = validator;
        this.templates = templates;
        this.starterFactory = starterFactory;
        this.businesses = businesses;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Optional<Website> find(UUID businessId) {
        return websites.findByBusinessId(businessId);
    }

    @Transactional(readOnly = true)
    public Website require(UUID businessId) {
        return find(businessId).orElseThrow(() -> new ResourceNotFoundException("Website"));
    }

    @Transactional(readOnly = true)
    public Optional<Publication> currentPublication(Website website) {
        return Optional.ofNullable(website.getPublishedPublicationId())
                .flatMap(id -> publications.findByIdAndBusinessId(id, website.getBusinessId()));
    }

    /** Stores a freshly generated draft (AI or template), creating the website on first generation. */
    @Transactional
    public Website saveGeneratedDraft(UUID businessId, UUID actorUserId, SiteConfig config, DraftSource source) {
        requireValid(config);
        Website website = websites.findByBusinessId(businessId)
                .map(w -> {
                    w.replaceDraft(config, source);
                    return w;
                })
                .orElseGet(() -> websites.save(new Website(businessId, config, source)));
        businesses.advanceStage(businessId, SetupStage.WEBSITE_DRAFTED);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "website.draft_generated", "Website", website.getId(),
                Map.of("source", source.name(), "template", config.templateId())));
        return website;
    }

    /** Owner (or AI assistant on the owner's behalf) saves a draft; stale revisions are rejected. */
    @Transactional
    public Website updateDraft(UUID businessId, UUID actorUserId, SiteConfig config, int expectedRevision) {
        Website website = require(businessId);
        if (website.getDraftRevision() != expectedRevision) {
            throw new ConflictException("The draft was changed elsewhere. Reload to see the latest version.");
        }
        requireValid(config);
        website.replaceDraft(config, DraftSource.OWNER_EDITED);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "website.draft_updated", "Website", website.getId(),
                Map.of("revision", website.getDraftRevision())));
        return website;
    }

    @Transactional
    public Website switchTemplate(UUID businessId, UUID actorUserId, String templateId) {
        Website website = require(businessId);
        WebsiteTemplate template = templates.find(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template"));
        Business business = businesses.get(businessId);
        SiteConfig config = starterFactory.retemplate(website.getDraftConfig(), template, business);
        requireValid(config);
        website.replaceDraft(config, DraftSource.OWNER_EDITED);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "website.template_changed", "Website", website.getId(),
                Map.of("template", templateId)));
        return website;
    }

    public void requireValid(SiteConfig config) {
        List<String> problems = validator.validate(config);
        if (!problems.isEmpty()) {
            throw new InvalidSiteConfigException(problems);
        }
    }

    public List<String> problemsWith(SiteConfig config) {
        return validator.validate(config);
    }
}
