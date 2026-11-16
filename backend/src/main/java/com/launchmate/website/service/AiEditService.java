package com.launchmate.website.service;

import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.PlanLimits;
import com.launchmate.business.service.BusinessService;
import com.launchmate.shared.exception.BusinessRuleException;
import com.launchmate.shared.ratelimit.AiUsageGuard;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.domain.Website;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Produces an AI edit <em>proposal</em>. Nothing is saved: the owner reviews the preview and applies it
 * through the normal draft-save path, so AI edits get the same validation and audit as manual ones.
 */
@Service
public class AiEditService {

    private final WebsiteService websites;
    private final BusinessService businesses;
    private final WebsiteEditAssistant assistant;
    private final AiUsageGuard aiUsage;

    public AiEditService(WebsiteService websites, BusinessService businesses, WebsiteEditAssistant assistant,
                         AiUsageGuard aiUsage) {
        this.websites = websites;
        this.businesses = businesses;
        this.assistant = assistant;
        this.aiUsage = aiUsage;
    }

    public record Proposal(SiteConfig config, int baseRevision) {
    }

    public Proposal propose(UUID businessId, UUID userId, String instruction) {
        Business business = businesses.get(businessId);
        aiUsage.acquire(userId, PlanLimits.of(business.getPlan()).aiRequestsPerHour());
        Website website = websites.require(businessId);
        SiteConfig current = website.getDraftConfig();

        SiteConfig candidate = assistant.propose(business, current, instruction, List.of(), null);
        List<String> problems = websites.problemsWith(candidate);
        if (!problems.isEmpty()) {
            candidate = assistant.propose(business, current, instruction, problems, candidate);
            problems = websites.problemsWith(candidate);
        }
        if (!problems.isEmpty()) {
            throw new BusinessRuleException("The AI couldn't make that change within the website rules. Try rephrasing it.",
                    problems);
        }
        // The template is owned by the owner's explicit choice, never switched implicitly by an AI edit.
        SiteConfig pinned = new SiteConfig(current.templateId(), candidate.theme(), candidate.seo(), candidate.sections());
        return new Proposal(pinned, website.getDraftRevision());
    }
}
