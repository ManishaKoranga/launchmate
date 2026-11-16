package com.launchmate.catalogue.domain;

import com.launchmate.shared.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends TimestampedEntity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductKind kind;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "price_amount")
    private BigDecimal price;

    @Column(name = "price_note")
    private String priceNote;

    private String category;

    @Column(name = "image_key")
    private String imageKey;

    @Column(nullable = false)
    private boolean visible;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected Product() {
    }

    public Product(UUID businessId, ProductInput input, int sortOrder) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.sortOrder = sortOrder;
        apply(input);
    }

    public void apply(ProductInput in) {
        this.kind = in.kind();
        this.name = in.name().trim();
        this.description = blankToNull(in.description());
        this.price = in.price();
        this.priceNote = blankToNull(in.priceNote());
        this.category = blankToNull(in.category());
        this.visible = in.visible() == null || in.visible();
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public ProductKind getKind() { return kind; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getPriceNote() { return priceNote; }
    public String getCategory() { return category; }
    public String getImageKey() { return imageKey; }
    public boolean isVisible() { return visible; }
    public int getSortOrder() { return sortOrder; }
}
