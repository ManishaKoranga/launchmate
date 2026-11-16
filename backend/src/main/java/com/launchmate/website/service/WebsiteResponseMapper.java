package com.launchmate.website.service;

import com.launchmate.business.service.BusinessService;
import com.launchmate.website.domain.Publication;
import com.launchmate.website.domain.Website;
import com.launchmate.website.dto.WebsiteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class WebsiteResponseMapper {

    private final WebsiteService websiteService;
    private final BusinessService businessService;
    private final String publicBaseUrl;

    public WebsiteResponseMapper(WebsiteService websiteService, BusinessService businessService,
                                 @Value("${launchmate.public-base-url}") String publicBaseUrl) {
        this.websiteService = websiteService;
        this.businessService = businessService;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional(readOnly = true)
    public WebsiteResponse toResponse(Website w) {
        Optional<Publication> published = websiteService.currentPublication(w);
        String slug = businessService.get(w.getBusinessId()).getSlug();
        return new WebsiteResponse(w.getId(), w.getDraftConfig(), w.getDraftRevision(), w.getDraftSource(),
                w.getUpdatedAt(),
                published.map(p -> new WebsiteResponse.PublishedInfo(p.getVersion(), p.getCreatedAt())).orElse(null),
                published.map(p -> !p.getConfig().equals(w.getDraftConfig())).orElse(true),
                publicBaseUrl + "/s/" + slug);
    }
}
