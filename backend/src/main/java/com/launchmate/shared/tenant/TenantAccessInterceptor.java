package com.launchmate.shared.tenant;

import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

/**
 * Guards every {@code /api/businesses/{businessId}/**} endpoint: the business id in the path is
 * only a selector; access is granted only if the authenticated user is a member. Non-members get
 * 404 so the existence of other tenants is not revealed.
 */
@Component
public class TenantAccessInterceptor implements HandlerInterceptor {

    private final TenantMembershipLookup membershipLookup;
    private final TenantContext tenantContext;

    public TenantAccessInterceptor(TenantMembershipLookup membershipLookup, TenantContext tenantContext) {
        this.membershipLookup = membershipLookup;
        this.tenantContext = tenantContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        @SuppressWarnings("unchecked")
        Map<String, String> vars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (vars == null || !vars.containsKey("businessId")) {
            return true; // e.g. POST /api/businesses (create) — not tenant scoped
        }
        UUID businessId = parse(vars.get("businessId"));
        AuthenticatedUser user = currentUser();
        MemberRole role = membershipLookup.findRole(businessId, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Business"));
        tenantContext.set(businessId, user.id(), role);
        return true;
    }

    private static UUID parse(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Business");
        }
    }

    private static AuthenticatedUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Tenant-scoped endpoint reached without an authenticated user");
        }
        return user;
    }
}
