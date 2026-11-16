package com.launchmate.business.dto;

import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.business.domain.SetupStage;

import java.util.UUID;

public record BusinessSummary(UUID id, String slug, String name, BusinessCategory category, SetupStage setupStage) {

    public static BusinessSummary from(Business b) {
        return new BusinessSummary(b.getId(), b.getSlug(), b.getName(), b.getCategory(), b.getSetupStage());
    }
}
