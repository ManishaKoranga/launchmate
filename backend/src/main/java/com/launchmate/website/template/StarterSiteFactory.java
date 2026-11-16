package com.launchmate.website.template;

import com.launchmate.business.domain.Business;
import com.launchmate.website.domain.Section;
import com.launchmate.website.domain.SectionType;
import com.launchmate.website.domain.Seo;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.domain.Theme;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a valid {@link SiteConfig} from a template and the business profile without AI, and
 * re-applies a different template to an existing config while keeping the owner's content.
 */
@Component
public class StarterSiteFactory {

    public SiteConfig create(WebsiteTemplate t, Business b) {
        StarterCopy c = t.starterCopy();
        List<Section> sections = new ArrayList<>();
        for (SectionType type : t.defaultSections()) {
            sections.add(section(type, t, b, c));
        }
        String place = b.getCity() == null ? "" : " in " + b.getCity();
        String title = truncate(b.getName() + place, 70);
        String description = truncate(b.getDescription() != null ? b.getDescription()
                : fill(c.heroSubheading(), b), 170);
        return new SiteConfig(t.id(), t.defaultTheme(), new Seo(title, description), sections);
    }

    /** Switches template: adopts its theme and section order, keeping existing copy for matching section types. */
    public SiteConfig retemplate(SiteConfig current, WebsiteTemplate t, Business b) {
        Map<SectionType, Section> existing = new LinkedHashMap<>();
        current.sections().forEach(s -> existing.put(s.type(), s));
        List<Section> sections = new ArrayList<>();
        for (SectionType type : t.defaultSections()) {
            sections.add(existing.containsKey(type) ? existing.remove(type) : section(type, t, b, t.starterCopy()));
        }
        existing.values().stream().filter(s -> t.allowedSections().contains(s.type()))
                .forEach(s -> sections.add(sections.size() - 1, s)); // keep extras, before CONTACT
        Theme theme = t.defaultTheme();
        return current.withTemplate(t.id(), theme, sections);
    }

    public Section section(SectionType type, WebsiteTemplate t, Business b, StarterCopy c) {
        return switch (type) {
            case HERO -> new Section("hero", type, true, b.getName(),
                    b.getTagline() != null ? b.getTagline() : fill(c.heroSubheading(), b), b.getDescription(), c.heroCta(), List.of());
            case ABOUT -> new Section("about", type, true, fill(c.aboutHeading(), b), null,
                    b.getDescription() != null ? b.getDescription() : fill(c.aboutBody(), b), null, List.of());
            case HIGHLIGHTS -> new Section("highlights", type, true, c.highlightsHeading(), null, null, null, c.highlights());
            case CATALOGUE -> new Section("catalogue", type, true, c.catalogueHeading(), c.catalogueSubheading(), null, null, List.of());
            case PROCESS -> new Section("process", type, true, c.processHeading(), null, null, null, c.process());
            case FAQ -> new Section("faq", type, true, c.faqHeading(), null, null, null, c.faq());
            case CTA_BANNER -> new Section("cta", type, true, c.ctaBannerHeading(), c.ctaBannerBody(), null, c.heroCta(), List.of());
            case HOURS_LOCATION -> new Section("hours", type, true, "Hours & location", null, null, null, List.of());
            case CONTACT -> new Section("contact", type, true, c.contactHeading(), c.contactSubheading(), null, "Send inquiry", List.of());
        };
    }

    private static String fill(String template, Business b) {
        String city = b.getCity() == null ? "your city" : b.getCity();
        return template.replace("{name}", b.getName()).replace("{city}", city)
                .replace("{area}", b.getArea() == null ? city : b.getArea())
                .replace(" in your city", "");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1).trim() + "…";
    }

}
