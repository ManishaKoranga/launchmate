package com.launchmate.business.repository;

import com.launchmate.business.domain.BusinessMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, BusinessMember.Key> {

    Optional<BusinessMember> findByBusinessIdAndUserId(UUID businessId, UUID userId);

    List<BusinessMember> findByUserId(UUID userId);

    long countByUserId(UUID userId);
}
