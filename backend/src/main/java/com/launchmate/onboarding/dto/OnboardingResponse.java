package com.launchmate.onboarding.dto;

import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.catalogue.domain.ProductInput;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OnboardingResponse(UUID id, BusinessProfile profile, List<ProductInput> offerings,
                                 List<String> missingFields, boolean readyForReview, UUID businessId,
                                 List<Message> messages, boolean aiAvailable) {

    public record Message(String role, String content, Instant createdAt) {
    }
}
