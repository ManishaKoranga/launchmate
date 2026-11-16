package com.launchmate.website.dto;

import com.launchmate.website.domain.DraftSource;
import com.launchmate.website.domain.SiteConfig;

import java.time.Instant;
import java.util.UUID;

public record WebsiteResponse(
        UUID id,
        SiteConfig draft,
        int draftRevision,
        DraftSource draftSource,
        Instant draftUpdatedAt,
        PublishedInfo published,
        boolean hasUnpublishedChanges,
        String publicUrl) {

    public record PublishedInfo(int version, Instant publishedAt) {
    }
}
