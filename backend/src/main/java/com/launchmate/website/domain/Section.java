package com.launchmate.website.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * One section of the page. A flat shape (rather than one record per type) keeps the JSON schema
 * simple for the model and the editor; unused fields are empty for a given type.
 */
public record Section(
        @JsonPropertyDescription("Stable lowercase id, e.g. 'hero', 'about', 'faq'.")
        @NotNull @Pattern(regexp = "^[a-z0-9-]{1,40}$", message = "must be lowercase letters, digits or dashes") String id,
        @NotNull SectionType type,
        boolean visible,
        @JsonPropertyDescription("Section heading, at most 120 characters.")
        @Size(max = 120) String heading,
        @JsonPropertyDescription("Short supporting line, at most 300 characters.")
        @Size(max = 300) String subheading,
        @JsonPropertyDescription("Paragraph text, at most 2000 characters. Plain text only.")
        @Size(max = 2000) String body,
        @JsonPropertyDescription("Button label, at most 40 characters.")
        @Size(max = 40) String ctaLabel,
        @JsonPropertyDescription("Highlights, process steps or FAQ entries. At most 12.")
        @Size(max = 12) List<@Valid @NotNull SectionItem> items) {

    public Section {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
