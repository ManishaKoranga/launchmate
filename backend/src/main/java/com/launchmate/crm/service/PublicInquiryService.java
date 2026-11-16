package com.launchmate.crm.service;

import com.launchmate.business.domain.Business;
import com.launchmate.business.repository.BusinessRepository;
import com.launchmate.crm.domain.LeadSource;
import com.launchmate.crm.dto.InquiryRequest;
import com.launchmate.shared.exception.RateLimitedException;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.shared.ratelimit.SlidingWindowLimiter;
import com.launchmate.website.service.WebsiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;

/**
 * Accepts inquiries from anonymous visitors of published websites. Defences: only published sites
 * accept inquiries, per-IP and per-business rate limits, a honeypot field, and full validation.
 */
@Service
public class PublicInquiryService {

    private static final Logger log = LoggerFactory.getLogger(PublicInquiryService.class);
    static final int PER_IP_PER_HOUR = 10;
    static final int PER_BUSINESS_PER_HOUR = 200;

    private final BusinessRepository businesses;
    private final WebsiteService websites;
    private final LeadService leads;
    private final SlidingWindowLimiter limiter;

    public PublicInquiryService(BusinessRepository businesses, WebsiteService websites, LeadService leads, Clock clock) {
        this.businesses = businesses;
        this.websites = websites;
        this.leads = leads;
        this.limiter = new SlidingWindowLimiter(Duration.ofHours(1), clock);
    }

    /** @return false if the submission was silently discarded as spam */
    @Transactional
    public boolean submit(String slug, InquiryRequest request, String clientIp) {
        Business business = businesses.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("Website"));
        boolean published = websites.find(business.getId()).map(w -> w.getPublishedPublicationId() != null).orElse(false);
        if (!published) {
            throw new ResourceNotFoundException("Website");
        }
        if (request.website() != null && !request.website().isBlank()) {
            log.info("Discarded honeypot inquiry for business {}", business.getId());
            return false;
        }
        if (!limiter.tryAcquire("ip:" + clientIp + ":" + slug, PER_IP_PER_HOUR)
                || !limiter.tryAcquire("biz:" + business.getId(), PER_BUSINESS_PER_HOUR)) {
            throw new RateLimitedException("Too many inquiries right now. Please try again later or contact the business directly.");
        }
        leads.create(business.getId(), LeadSource.WEBSITE, request, null);
        return true;
    }
}
