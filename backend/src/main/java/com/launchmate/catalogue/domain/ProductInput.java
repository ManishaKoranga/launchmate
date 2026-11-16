package com.launchmate.catalogue.domain;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Validated product data shared by the REST API, onboarding and AI tools. Prices are in INR. */
public record ProductInput(
        @NotNull ProductKind kind,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @DecimalMin("0") @DecimalMax("10000000") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @Size(max = 60) String priceNote,
        @Size(max = 60) String category,
        Boolean visible) {
}
