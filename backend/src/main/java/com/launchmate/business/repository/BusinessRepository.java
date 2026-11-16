package com.launchmate.business.repository;

import com.launchmate.business.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    boolean existsBySlug(String slug);

    Optional<Business> findBySlug(String slug);

    @Query("""
            select b from Business b
            where exists (select 1 from BusinessMember m where m.businessId = b.id and m.userId = :userId)
            order by b.createdAt
            """)
    List<Business> findAllForMember(@Param("userId") UUID userId);
}
