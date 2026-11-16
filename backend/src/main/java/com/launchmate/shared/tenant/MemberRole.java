package com.launchmate.shared.tenant;

public enum MemberRole {
    OWNER,
    STAFF;

    public boolean canManageBusiness() {
        return this == OWNER;
    }
}
