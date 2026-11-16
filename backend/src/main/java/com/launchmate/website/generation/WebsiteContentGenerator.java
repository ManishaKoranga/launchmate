package com.launchmate.website.generation;

import com.launchmate.business.domain.Business;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.template.WebsiteTemplate;

import java.util.List;

/**
 * Port for AI website content generation. The website module depends on this interface; the AI
 * module provides the Claude implementation (dependency inversion — no website→AI-vendor coupling).
 */
public interface WebsiteContentGenerator {

    /**
     * @param correction problems found in a previous attempt, empty on the first attempt
     */
    SiteConfig generate(Business business, WebsiteTemplate template, List<String> catalogueNames,
                        SiteConfig previousAttempt, List<String> correction);
}
