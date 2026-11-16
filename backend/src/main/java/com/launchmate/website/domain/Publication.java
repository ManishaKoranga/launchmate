package com.launchmate.website.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** An immutable snapshot of a website configuration at the moment it was published. */
@Entity
@Immutable
@Table(name = "publications")
public class Publication {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "website_id", nullable = false)
    private UUID websiteId;

    @Column(nullable = false)
    private int version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private SiteConfig config;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Publication() {
    }

    public Publication(Website website, int version, UUID publishedBy, Instant now) {
        this.id = UUID.randomUUID();
        this.businessId = website.getBusinessId();
        this.websiteId = website.getId();
        this.version = version;
        this.config = website.getDraftConfig();
        this.publishedBy = publishedBy;
        this.createdAt = now;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getWebsiteId() { return websiteId; }
    public int getVersion() { return version; }
    public SiteConfig getConfig() { return config; }
    public UUID getPublishedBy() { return publishedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
