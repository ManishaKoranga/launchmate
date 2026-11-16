package com.launchmate.website.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A highlight, process step or FAQ entry (title = question, body = answer for FAQ). */
public record SectionItem(
        @JsonPropertyDescription("Short title, at most 100 characters.")
        @NotBlank @Size(max = 100) String title,
        @JsonPropertyDescription("Supporting text, at most 600 characters.")
        @Size(max = 600) String body) {
}
