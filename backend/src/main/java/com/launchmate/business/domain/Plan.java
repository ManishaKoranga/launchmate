package com.launchmate.business.domain;

/** Subscription tier. Entitlement checks read from here; no payment provider is integrated yet. */
public enum Plan {
    FREE,
    STARTER,
    GROWTH,
    BUSINESS
}
