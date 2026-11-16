package com.launchmate.ai.workflows;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Structured output schema for onboarding extraction. All strings; "" means unknown. */
record OnboardingExtraction(Profile profile, List<Offering> offerings, String reply) {

    record Profile(
            String name,
            @JsonPropertyDescription("One of BAKERY_FOOD, BEAUTY_SALON, HOME_SERVICES, EDUCATION_TUTORING, HANDMADE_RETAIL, PROFESSIONAL_SERVICES, or empty if unknown")
            String category,
            String tagline,
            String description,
            String city,
            String area,
            String phone,
            String whatsapp,
            String email,
            String targetCustomers,
            String languages) {
    }

    record Offering(
            String name,
            @JsonPropertyDescription("PRODUCT or SERVICE") String kind,
            String description,
            @JsonPropertyDescription("Price in Indian rupees as stated by the owner, or 0 if not stated") long priceInRupees,
            String priceNote) {
    }
}
