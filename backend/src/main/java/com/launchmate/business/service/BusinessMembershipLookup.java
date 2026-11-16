package com.launchmate.business.service;

import com.launchmate.business.domain.BusinessMember;
import com.launchmate.business.repository.BusinessMemberRepository;
import com.launchmate.shared.tenant.MemberRole;
import com.launchmate.shared.tenant.TenantMembershipLookup;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
class BusinessMembershipLookup implements TenantMembershipLookup {

    private final BusinessMemberRepository members;

    BusinessMembershipLookup(BusinessMemberRepository members) {
        this.members = members;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberRole> findRole(UUID businessId, UUID userId) {
        return members.findByBusinessIdAndUserId(businessId, userId).map(BusinessMember::getRole);
    }
}
