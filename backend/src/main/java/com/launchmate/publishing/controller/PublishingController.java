package com.launchmate.publishing.controller;

import com.launchmate.publishing.dto.PublicSiteResponse;
import com.launchmate.publishing.service.PublicSiteService;
import com.launchmate.publishing.service.PublishingService;
import com.launchmate.shared.tenant.TenantContext;
import com.launchmate.website.dto.WebsiteResponse;
import com.launchmate.website.service.WebsiteResponseMapper;
import com.launchmate.website.service.WebsiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Publishing")
public class PublishingController {

    private final PublishingService publishingService;
    private final PublicSiteService publicSites;
    private final WebsiteService websiteService;
    private final WebsiteResponseMapper mapper;
    private final TenantContext tenant;

    public PublishingController(PublishingService publishingService, PublicSiteService publicSites,
                                WebsiteService websiteService, WebsiteResponseMapper mapper, TenantContext tenant) {
        this.publishingService = publishingService;
        this.publicSites = publicSites;
        this.websiteService = websiteService;
        this.mapper = mapper;
        this.tenant = tenant;
    }

    @Operation(summary = "Publish the current draft as a new version")
    @PostMapping("/api/businesses/{businessId}/website/publish")
    public WebsiteResponse publish(@PathVariable UUID businessId) {
        publishingService.publish(tenant.businessId(), tenant.userId());
        return mapper.toResponse(websiteService.require(tenant.businessId()));
    }

    @Operation(summary = "Render model of the draft, for the owner's preview")
    @GetMapping("/api/businesses/{businessId}/website/preview")
    public PublicSiteResponse preview(@PathVariable UUID businessId) {
        return publicSites.draftPreview(tenant.businessId());
    }

    @Operation(summary = "Published website by LaunchMate slug (public)")
    @GetMapping("/api/public/sites/{slug}")
    public PublicSiteResponse publicSite(@PathVariable String slug) {
        return publicSites.publishedBySlug(slug);
    }
}
