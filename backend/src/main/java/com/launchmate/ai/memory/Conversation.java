package com.launchmate.ai.memory;

import com.launchmate.shared.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** A persisted AI conversation, always owned by one user and (once it exists) one business. */
@Entity
@Table(name = "conversations")
public class Conversation extends TimestampedEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "business_id")
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationKind kind;

    /** Workflow-specific state (e.g. onboarding draft). */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> state;

    protected Conversation() {
    }

    public Conversation(UUID userId, UUID businessId, ConversationKind kind) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.businessId = businessId;
        this.kind = kind;
        this.state = new HashMap<>();
    }

    public void replaceState(Map<String, Object> newState) {
        this.state = new HashMap<>(newState);
    }

    public void linkBusiness(UUID businessId) {
        if (this.businessId != null && !this.businessId.equals(businessId)) {
            throw new IllegalStateException("Conversation already belongs to another business");
        }
        this.businessId = businessId;
    }

    /** Touch so "most recent conversation" ordering reflects new messages. */
    public void touch() {
        this.state = new HashMap<>(state == null ? Map.of() : state);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getBusinessId() { return businessId; }
    public ConversationKind getKind() { return kind; }
    public Map<String, Object> getState() { return state == null ? Map.of() : state; }
}
