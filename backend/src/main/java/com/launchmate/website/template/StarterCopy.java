package com.launchmate.website.template;

import com.launchmate.website.domain.SectionItem;

import java.util.List;

/**
 * Template-authored starter content used when AI generation is unavailable or not wanted. Strings may
 * contain {name}, {city} and {area} placeholders. Never contains invented facts (prices, reviews,
 * years in business).
 */
public record StarterCopy(
        String heroSubheading,
        String heroCta,
        String aboutHeading,
        String aboutBody,
        String highlightsHeading,
        List<SectionItem> highlights,
        String catalogueHeading,
        String catalogueSubheading,
        String processHeading,
        List<SectionItem> process,
        String faqHeading,
        List<SectionItem> faq,
        String ctaBannerHeading,
        String ctaBannerBody,
        String contactHeading,
        String contactSubheading) {
}
