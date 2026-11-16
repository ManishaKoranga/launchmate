package com.launchmate.publishing.service;

import com.launchmate.business.domain.Business;
import com.launchmate.business.repository.BusinessRepository;
import com.launchmate.catalogue.dto.ProductResponse;
import com.launchmate.catalogue.service.ProductImageUrls;
import com.launchmate.catalogue.service.ProductService;
import com.launchmate.publishing.dto.PublicSiteResponse;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.website.domain.Publication;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.domain.Website;
import com.launchmate.website.service.WebsiteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Builds the read model used to render public websites and owner previews. */
@Service
public class PublicSiteService {

    private final BusinessRepository businesses;
    private final WebsiteService websites;
    private final ProductService products;
    private final ProductImageUrls imageUrls;

    public PublicSiteService(BusinessRepository businesses, WebsiteService websites, ProductService products,
                             ProductImageUrls imageUrls) {
        this.businesses = businesses;
        this.websites = websites;
        this.products = products;
        this.imageUrls = imageUrls;
    }

    /** The published site for a slug; unpublished sites are indistinguishable from missing ones. */
    @Transactional(readOnly = true)
    public PublicSiteResponse publishedBySlug(String slug) {
        Business business = businesses.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("Website"));
        return published(business);
    }

    @Transactional(readOnly = true)
    public PublicSiteResponse publishedForBusiness(UUID businessId) {
        Business business = businesses.findById(businessId).orElseThrow(() -> new ResourceNotFoundException("Website"));
        return published(business);
    }

    /** Owner-only preview of the current draft (caller must have verified tenant access). */
    @Transactional(readOnly = true)
    public PublicSiteResponse draftPreview(UUID businessId) {
        Business business = businesses.findById(businessId).orElseThrow(() -> new ResourceNotFoundException("Business"));
        Website website = websites.require(businessId);
        return build(business, website.getDraftConfig(), website.getDraftRevision(), website.getUpdatedAt(), true);
    }

    private PublicSiteResponse published(Business business) {
        Website website = websites.find(business.getId()).orElseThrow(() -> new ResourceNotFoundException("Website"));
        Publication publication = websites.currentPublication(website)
                .orElseThrow(() -> new ResourceNotFoundException("Website"));
        return build(business, publication.getConfig(), publication.getVersion(), publication.getCreatedAt(), false);
    }

    private PublicSiteResponse build(Business b, SiteConfig config, int version, Instant at, boolean preview) {
        List<PublicSiteResponse.PublicProduct> items = products.listVisible(b.getId()).stream()
                .map(imageUrls::toResponse)
                .map(PublicSiteService::toPublic)
                .toList();
        var business = new PublicSiteResponse.PublicBusiness(b.getName(), b.getCategory(), b.getTagline(),
                b.getDescription(), b.getCity(), b.getArea(), b.getAddress(), b.getPhone(), b.getWhatsapp(),
                b.getEmail(), b.getOpeningHours());
        return new PublicSiteResponse(b.getSlug(), business, config, items, version, at, preview);
    }

    private static PublicSiteResponse.PublicProduct toPublic(ProductResponse p) {
        return new PublicSiteResponse.PublicProduct(p.id(), p.kind(), p.name(), p.description(), p.price(),
                p.priceNote(), p.category(), p.imageUrl());
    }
}
