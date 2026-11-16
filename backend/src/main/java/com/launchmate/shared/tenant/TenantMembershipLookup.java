package com.launchmate.shared.tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Port used by the tenant interceptor to check membership. Implemented by the business module so
 * that {@code shared} does not depend on business persistence (dependency inversion).
 */
public interface TenantMembershipLookup {

    Optional<MemberRole> findRole(UUID businessId, UUID userId);
}
