package com.launchmate.shared.audit;

import java.util.Map;
import java.util.UUID;

/**
 * An auditable action. {@code details} must not contain secrets or full customer messages.
 */
public record AuditEvent(UUID businessId,
                         UUID actorUserId,
                         ActorType actorType,
                         String action,
                         String entityType,
                         String entityId,
                         Map<String, Object> details) {

    public static AuditEvent byUser(UUID businessId, UUID userId, String action, String entityType, Object entityId,
                                    Map<String, Object> details) {
        return new AuditEvent(businessId, userId, ActorType.USER, action, entityType,
                entityId == null ? null : entityId.toString(), details);
    }
}
