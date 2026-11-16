package com.launchmate.onboarding.domain;

import com.launchmate.business.domain.BusinessProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * What must be known before a website can be built. Computed by code, not trusted from the model,
 * so the "ready" signal is deterministic.
 */
public final class RequiredFields {

    public static final List<String> ALL = List.of("name", "category", "city", "contact");

    private RequiredFields() {
    }

    public static List<String> missing(BusinessProfile p) {
        if (p == null) {
            return ALL;
        }
        List<String> missing = new ArrayList<>();
        if (blank(p.name())) missing.add("name");
        if (p.category() == null) missing.add("category");
        if (blank(p.city())) missing.add("city");
        if (blank(p.phone()) && blank(p.whatsapp())) missing.add("contact");
        return missing;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
