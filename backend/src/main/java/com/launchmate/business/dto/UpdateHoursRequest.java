package com.launchmate.business.dto;

import com.launchmate.business.domain.DailyHours;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateHoursRequest(@NotNull @Valid @Size(max = 7) List<DailyHours> openingHours) {
}
