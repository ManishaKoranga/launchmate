package com.launchmate.website.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record Theme(
        @JsonPropertyDescription("Brand colour as #RRGGBB. Must be dark enough for white text on top of it.")
        @NotNull @Pattern(regexp = SiteConfig.HEX_COLOUR, message = "must be a #RRGGBB colour") String primaryColor,
        @JsonPropertyDescription("Secondary highlight colour as #RRGGBB.")
        @NotNull @Pattern(regexp = SiteConfig.HEX_COLOUR, message = "must be a #RRGGBB colour") String accentColor,
        @NotNull ThemePalette palette,
        @NotNull FontPairing fontPairing,
        @NotNull CornerStyle corners) {
}
