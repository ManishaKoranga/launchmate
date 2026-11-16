package com.launchmate.business.dto;

import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.business.domain.DailyHours;
import com.launchmate.business.domain.Plan;
import com.launchmate.business.domain.SetupStage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BusinessResponse(
        UUID id,
        String slug,
        String name,
        BusinessCategory category,
        String tagline,
        String description,
        String city,
        String area,
        String address,
        String phone,
        String whatsapp,
        String email,
        String targetCustomers,
        String languages,
        List<DailyHours> openingHours,
        Plan plan,
        SetupStage setupStage,
        Instant createdAt) {

    public static BusinessResponse from(Business b) {
        return new BusinessResponse(b.getId(), b.getSlug(), b.getName(), b.getCategory(), b.getTagline(),
                b.getDescription(), b.getCity(), b.getArea(), b.getAddress(), b.getPhone(), b.getWhatsapp(),
                b.getEmail(), b.getTargetCustomers(), b.getLanguages(), b.getOpeningHours(), b.getPlan(),
                b.getSetupStage(), b.getCreatedAt());
    }
}
