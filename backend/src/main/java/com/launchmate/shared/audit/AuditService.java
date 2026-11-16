package com.launchmate.shared.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Records audit events in the caller's transaction, so an audit row exists if and only if the
 * audited change was committed.
 */
@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final Clock clock;

    public AuditService(AuditLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditEvent event) {
        repository.save(new AuditLog(event.businessId(), event.actorUserId(), event.actorType(), event.action(),
                event.entityType(), event.entityId(), event.details(), clock.instant()));
    }
}
