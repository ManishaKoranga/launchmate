package com.launchmate.publishing.dto;

import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.business.domain.DailyHours;
import com.launchmate.catalogue.domain.ProductKind;
import com.launchmate.website.domain.SiteConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Everything a public website needs, and nothing more: no internal ids beyond what the inquiry form
 * needs, no owner data, no drafts.
 */
public record PublicSiteResponse(
        String slug,
        PublicBusiness business,
        SiteConfig config,
        List<PublicProduct> products,
        int version,
        Instant publishedAt,
        boolean preview) {

    public record PublicBusiness(String name, BusinessCategory category, String tagline, String description,
                                 String city, String area, String address, String phone, String whatsapp,
                                 String email, List<DailyHours> openingHours) {
    }

    public record PublicProduct(UUID id, ProductKind kind, String name, String description, BigDecimal price,
                                String priceNote, String category, String imageUrl) {
    }
}
