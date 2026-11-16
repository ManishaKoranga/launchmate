package com.launchmate.business.domain;

/** Ordered onboarding milestones; a business only moves forward. */
public enum SetupStage {
    PROFILE_DRAFT,
    PROFILE_CONFIRMED,
    WEBSITE_DRAFTED,
    PUBLISHED;

    public boolean isBefore(SetupStage other) {
        return ordinal() < other.ordinal();
    }
}
