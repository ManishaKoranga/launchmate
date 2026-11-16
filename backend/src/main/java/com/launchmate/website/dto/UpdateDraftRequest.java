package com.launchmate.website.dto;

import com.launchmate.website.domain.SiteConfig;
import jakarta.validation.constraints.NotNull;

/** Full draft replacement. {@code expectedRevision} provides optimistic concurrency. */
public record UpdateDraftRequest(@NotNull SiteConfig config, @NotNull Integer expectedRevision) {
}
