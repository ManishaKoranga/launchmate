package com.launchmate.catalogue.dto;

import com.launchmate.catalogue.domain.Product;
import com.launchmate.catalogue.domain.ProductKind;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(UUID id, ProductKind kind, String name, String description, BigDecimal price,
                              String priceNote, String category, String imageUrl, boolean visible, int sortOrder,
                              Instant updatedAt) {

    public static ProductResponse from(Product p, String imageUrl) {
        return new ProductResponse(p.getId(), p.getKind(), p.getName(), p.getDescription(), p.getPrice(),
                p.getPriceNote(), p.getCategory(), imageUrl, p.isVisible(), p.getSortOrder(), p.getUpdatedAt());
    }
}
