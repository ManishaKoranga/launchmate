package com.launchmate.business.domain;

import com.launchmate.shared.tenant.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_members")
@IdClass(BusinessMember.Key.class)
public class BusinessMember {

    @Id
    @Column(name = "business_id")
    private UUID businessId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BusinessMember() {
    }

    public BusinessMember(UUID businessId, UUID userId, MemberRole role) {
        this.businessId = businessId;
        this.userId = userId;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public UUID getBusinessId() { return businessId; }
    public UUID getUserId() { return userId; }
    public MemberRole getRole() { return role; }

    /** Composite primary key (business_id, user_id). */
    public static class Key implements Serializable {
        private UUID businessId;
        private UUID userId;

        public Key() {
        }

        public Key(UUID businessId, UUID userId) {
            this.businessId = businessId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && java.util.Objects.equals(businessId, k.businessId)
                    && java.util.Objects.equals(userId, k.userId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(businessId, userId);
        }
    }
}
