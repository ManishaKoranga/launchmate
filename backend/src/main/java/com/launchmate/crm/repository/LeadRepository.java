package com.launchmate.crm.repository;

import com.launchmate.crm.domain.Lead;
import com.launchmate.crm.domain.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Optional<Lead> findByIdAndBusinessId(UUID id, UUID businessId);

    Page<Lead> findByBusinessId(UUID businessId, Pageable pageable);

    Page<Lead> findByBusinessIdAndStatus(UUID businessId, LeadStatus status, Pageable pageable);

    @Query("select l.status as status, count(l) as count from Lead l where l.businessId = :businessId group by l.status")
    List<StatusCount> countByStatus(@Param("businessId") UUID businessId);

    long countByBusinessIdAndCreatedAtAfter(UUID businessId, Instant after);

    interface StatusCount {
        LeadStatus getStatus();
        long getCount();
    }
}
