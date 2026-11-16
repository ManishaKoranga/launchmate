package com.launchmate.onboarding.domain;

import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.catalogue.domain.ProductInput;

import java.util.List;
import java.util.UUID;

/**
 * The evolving onboarding draft. The profile is unvalidated while drafting; it is validated only when
 * the owner approves it.
 */
public record OnboardingState(BusinessProfile profile, List<ProductInput> offerings, List<String> missingFields,
                              boolean readyForReview, UUID businessId) {

    public static OnboardingState empty() {
        return new OnboardingState(null, List.of(), RequiredFields.ALL, false, null);
    }

    public boolean isCompleted() {
        return businessId != null;
    }
}
