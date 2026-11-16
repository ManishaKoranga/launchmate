package com.launchmate.business.service;

import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.BusinessMember;
import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.business.domain.DailyHours;
import com.launchmate.business.domain.SetupStage;
import com.launchmate.business.repository.BusinessMemberRepository;
import com.launchmate.business.repository.BusinessRepository;
import com.launchmate.shared.audit.AuditEvent;
import com.launchmate.shared.audit.AuditService;
import com.launchmate.shared.exception.BusinessRuleException;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.shared.tenant.MemberRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Business profile use cases. Callers pass an already-authorised business id (resolved by the
 * tenant interceptor or the AI tool executor); this service never trusts ids from request bodies.
 */
@Service
public class BusinessService {

    static final int MAX_BUSINESSES_PER_USER = 5;

    private final BusinessRepository businesses;
    private final BusinessMemberRepository members;
    private final SlugGenerator slugGenerator;
    private final AuditService audit;

    public BusinessService(BusinessRepository businesses, BusinessMemberRepository members,
                           SlugGenerator slugGenerator, AuditService audit) {
        this.businesses = businesses;
        this.members = members;
        this.slugGenerator = slugGenerator;
        this.audit = audit;
    }

    @Transactional
    public Business create(UUID ownerUserId, BusinessProfile profile) {
        if (members.countByUserId(ownerUserId) >= MAX_BUSINESSES_PER_USER) {
            throw new BusinessRuleException("You can manage at most " + MAX_BUSINESSES_PER_USER + " businesses");
        }
        validateHours(profile.openingHours());
        Business business = businesses.save(new Business(slugGenerator.generate(profile.name(), profile.city()), profile));
        members.save(new BusinessMember(business.getId(), ownerUserId, MemberRole.OWNER));
        audit.record(AuditEvent.byUser(business.getId(), ownerUserId, "business.created", "Business",
                business.getId(), Map.of("slug", business.getSlug())));
        return business;
    }

    @Transactional(readOnly = true)
    public List<Business> listForUser(UUID userId) {
        return businesses.findAllForMember(userId);
    }

    @Transactional(readOnly = true)
    public Business get(UUID businessId) {
        return businesses.findById(businessId).orElseThrow(() -> new ResourceNotFoundException("Business"));
    }

    @Transactional
    public Business updateProfile(UUID businessId, UUID actorUserId, BusinessProfile profile) {
        validateHours(profile.openingHours());
        Business business = get(businessId);
        business.applyProfile(profile);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "business.profile_updated", "Business", businessId, null));
        return business;
    }

    @Transactional
    public Business updateOpeningHours(UUID businessId, UUID actorUserId, List<DailyHours> hours) {
        validateHours(hours);
        Business business = get(businessId);
        business.replaceOpeningHours(hours);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "business.hours_updated", "Business", businessId, null));
        return business;
    }

    @Transactional
    public Business confirmProfile(UUID businessId, UUID actorUserId) {
        Business business = get(businessId);
        business.advanceTo(SetupStage.PROFILE_CONFIRMED);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "business.profile_confirmed", "Business", businessId, null));
        return business;
    }

    /** Used by other modules (website, publishing) to record onboarding progress. */
    @Transactional
    public void advanceStage(UUID businessId, SetupStage stage) {
        get(businessId).advanceTo(stage);
    }

    private static void validateHours(List<DailyHours> hours) {
        if (hours == null) {
            return;
        }
        Set<DayOfWeek> seen = EnumSet.noneOf(DayOfWeek.class);
        for (DailyHours h : hours) {
            if (!seen.add(h.day())) {
                throw new BusinessRuleException("Opening hours contain " + h.day() + " more than once");
            }
        }
    }
}
