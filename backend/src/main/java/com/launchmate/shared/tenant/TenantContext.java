package com.launchmate.shared.tenant;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

/**
 * The tenant verified for the current request. Populated only by {@link TenantAccessInterceptor}
 * after a membership check against the authenticated session user — never from raw input.
 */
@Component
@RequestScope
public class TenantContext {

    private UUID businessId;
    private UUID userId;
    private MemberRole role;

    void set(UUID businessId, UUID userId, MemberRole role) {
        if (this.businessId != null) {
            throw new IllegalStateException("Tenant context already initialised for this request");
        }
        this.businessId = businessId;
        this.userId = userId;
        this.role = role;
    }

    public UUID businessId() {
        return require(businessId);
    }

    public UUID userId() {
        return require(userId);
    }

    public MemberRole role() {
        return require(role);
    }

    private static <T> T require(T value) {
        if (value == null) {
            throw new IllegalStateException("No tenant resolved for this request");
        }
        return value;
    }
}
