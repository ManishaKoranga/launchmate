package com.launchmate.publishing.service;

import com.launchmate.business.domain.SetupStage;
import com.launchmate.business.service.BusinessService;
import com.launchmate.shared.audit.AuditEvent;
import com.launchmate.shared.audit.AuditService;
import com.launchmate.website.domain.Publication;
import com.launchmate.website.domain.Website;
import com.launchmate.website.repository.PublicationRepository;
import com.launchmate.website.service.WebsiteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

/** Publishes the current draft as a new immutable version. */
@Service
public class PublishingService {

    private final WebsiteService websites;
    private final PublicationRepository publications;
    private final BusinessService businesses;
    private final AuditService audit;
    private final Clock clock;

    public PublishingService(WebsiteService websites, PublicationRepository publications, BusinessService businesses,
                             AuditService audit, Clock clock) {
        this.websites = websites;
        this.publications = publications;
        this.businesses = businesses;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public Publication publish(UUID businessId, UUID actorUserId) {
        Website website = websites.require(businessId);
        websites.requireValid(website.getDraftConfig()); // re-check: validation rules may have changed since saving
        int version = publications.maxVersion(website.getId()) + 1;
        Publication publication = publications.save(new Publication(website, version, actorUserId, clock.instant()));
        website.markPublished(publication.getId());
        businesses.advanceStage(businessId, SetupStage.PUBLISHED);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "website.published", "Website", website.getId(),
                Map.of("version", version)));
        return publication;
    }
}
