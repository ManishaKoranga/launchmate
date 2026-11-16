package com.launchmate.crm.domain;

public enum LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    QUOTED,
    CONVERTED,
    LOST;

    public boolean isOpen() {
        return this != CONVERTED && this != LOST;
    }
}
