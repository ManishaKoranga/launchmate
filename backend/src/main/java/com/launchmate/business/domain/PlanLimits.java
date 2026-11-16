package com.launchmate.business.domain;

/**
 * Entitlements per subscription tier. Kept as data so adding a tier or changing a limit does not
 * touch the modules that enforce them.
 */
public record PlanLimits(int maxProducts, int aiRequestsPerHour, int customDomains) {

    public static PlanLimits of(Plan plan) {
        return switch (plan) {
            case FREE -> new PlanLimits(20, 40, 0);
            case STARTER -> new PlanLimits(100, 120, 1);
            case GROWTH -> new PlanLimits(500, 300, 1);
            case BUSINESS -> new PlanLimits(5000, 1000, 5);
        };
    }
}
