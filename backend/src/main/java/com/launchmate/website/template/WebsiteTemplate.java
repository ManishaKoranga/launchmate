package com.launchmate.website.template;

import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.website.domain.SectionType;
import com.launchmate.website.domain.Theme;

import java.util.List;
import java.util.Set;

/**
 * A reusable website template. Templates are data — adding one requires no renderer code, only a
 * new entry in {@link TemplateRegistry} (open/closed principle).
 */
public record WebsiteTemplate(
        String id,
        String name,
        BusinessCategory category,
        String description,
        Theme defaultTheme,
        List<SectionType> defaultSections,
        Set<SectionType> allowedSections,
        String writingGuidance,
        StarterCopy starterCopy) {
}
