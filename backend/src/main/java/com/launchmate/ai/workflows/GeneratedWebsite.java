package com.launchmate.ai.workflows;

import com.launchmate.website.domain.Section;
import com.launchmate.website.domain.Seo;
import com.launchmate.website.domain.Theme;

import java.util.List;

/** Structured output schema for website generation; the template id is chosen by code, not the model. */
public record GeneratedWebsite(Theme theme, Seo seo, List<Section> sections) {
}
