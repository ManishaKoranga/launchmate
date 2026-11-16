package com.launchmate.website.domain;

/**
 * Building blocks of a website. The renderer has one component per type. Contact details, hours
 * and catalogue items are read from live business data, not duplicated into the config.
 */
public enum SectionType {
    HERO,
    ABOUT,
    HIGHLIGHTS,
    CATALOGUE,
    PROCESS,
    FAQ,
    CTA_BANNER,
    HOURS_LOCATION,
    CONTACT
}
