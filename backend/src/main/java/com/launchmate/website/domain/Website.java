package com.launchmate.website.domain;

import com.launchmate.shared.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * A business's website: an editable draft plus a pointer to the currently published, immutable
 * {@link Publication}. Editing the draft never changes what visitors see.
 */
@Entity
@Table(name = "websites")
public class Website extends TimestampedEntity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "draft_config", nullable = false)
    private SiteConfig draftConfig;

    @Column(name = "draft_revision", nullable = false)
    private int draftRevision;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_source", nullable = false)
    private DraftSource draftSource;

    @Column(name = "published_publication_id")
    private UUID publishedPublicationId;

    protected Website() {
    }

    public Website(UUID businessId, SiteConfig config, DraftSource source) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.draftRevision = 0;
        replaceDraft(config, source);
    }

    /** Callers must validate the config first (see SiteConfigValidator). */
    public void replaceDraft(SiteConfig config, DraftSource source) {
        this.draftConfig = config;
        this.templateId = config.templateId();
        this.draftSource = source;
        this.draftRevision++;
    }

    public void markPublished(UUID publicationId) {
        this.publishedPublicationId = publicationId;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getTemplateId() { return templateId; }
    public SiteConfig getDraftConfig() { return draftConfig; }
    public int getDraftRevision() { return draftRevision; }
    public DraftSource getDraftSource() { return draftSource; }
    public UUID getPublishedPublicationId() { return publishedPublicationId; }
}
