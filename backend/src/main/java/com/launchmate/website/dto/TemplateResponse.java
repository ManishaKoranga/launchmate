package com.launchmate.website.dto;

import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.website.domain.Theme;
import com.launchmate.website.template.WebsiteTemplate;

public record TemplateResponse(String id, String name, BusinessCategory category, String description, Theme defaultTheme) {

    public static TemplateResponse from(WebsiteTemplate t) {
        return new TemplateResponse(t.id(), t.name(), t.category(), t.description(), t.defaultTheme());
    }
}
