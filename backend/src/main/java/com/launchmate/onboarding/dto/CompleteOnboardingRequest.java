package com.launchmate.onboarding.dto;

import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.catalogue.domain.ProductInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** The owner-approved (and possibly corrected) profile and offerings. */
public record CompleteOnboardingRequest(@NotNull @Valid BusinessProfile profile,
                                        @NotNull @Size(max = 20) List<@Valid @NotNull ProductInput> offerings) {
}
