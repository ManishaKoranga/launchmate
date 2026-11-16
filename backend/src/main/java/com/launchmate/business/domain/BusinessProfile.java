package com.launchmate.business.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * The editable business profile. Used by the REST API, onboarding approval and AI tools so that the
 * validation rules live in exactly one place.
 */
public record BusinessProfile(
        @NotBlank @Size(max = 120) String name,
        @NotNull BusinessCategory category,
        @Size(max = 200) String tagline,
        @Size(max = 4000) String description,
        @Size(max = 80) String city,
        @Size(max = 120) String area,
        @Size(max = 300) String address,
        @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "is not a valid phone number") String phone,
        @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "is not a valid phone number") String whatsapp,
        @Email @Size(max = 254) String email,
        @Size(max = 300) String targetCustomers,
        @Size(max = 120) String languages,
        @Valid @Size(max = 7) List<DailyHours> openingHours) {
}
