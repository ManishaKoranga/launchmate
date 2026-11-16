package com.launchmate.website.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Seo(
        @JsonPropertyDescription("Page title, at most 70 characters.")
        @NotBlank @Size(max = 70) String title,
        @JsonPropertyDescription("Meta description, at most 170 characters.")
        @NotBlank @Size(max = 170) String description) {
}
