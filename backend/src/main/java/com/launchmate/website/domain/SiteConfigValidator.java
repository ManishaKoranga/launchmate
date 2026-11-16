package com.launchmate.website.domain;

import com.launchmate.website.template.TemplateRegistry;
import com.launchmate.website.template.WebsiteTemplate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The single gate every {@link SiteConfig} passes before it is saved or rendered — whether it came
 * from Claude, the editor or the AI assistant. Returns human-readable problems (also fed back to the
 * model so it can correct itself).
 */
@Component
public class SiteConfigValidator {

    private static final double MIN_CONTRAST_WITH_WHITE = 4.5;

    private final Validator beanValidator;
    private final TemplateRegistry templates;

    public SiteConfigValidator(Validator beanValidator, TemplateRegistry templates) {
        this.beanValidator = beanValidator;
        this.templates = templates;
    }

    public List<String> validate(SiteConfig config) {
        List<String> problems = new ArrayList<>();
        if (config == null) {
            return List.of("config is required");
        }
        for (ConstraintViolation<SiteConfig> v : beanValidator.validate(config)) {
            problems.add(v.getPropertyPath() + " " + v.getMessage());
        }
        if (!problems.isEmpty()) {
            return problems; // structural problems make the rule checks below unreliable
        }

        Optional<WebsiteTemplate> template = templates.find(config.templateId());
        if (template.isEmpty()) {
            problems.add("templateId '" + config.templateId() + "' is not a known template");
            return problems;
        }
        Set<SectionType> allowed = template.get().allowedSections();
        Set<String> ids = new HashSet<>();
        Set<SectionType> types = EnumSet.noneOf(SectionType.class);
        for (Section s : config.sections()) {
            if (!ids.add(s.id())) {
                problems.add("section id '" + s.id() + "' is used more than once");
            }
            if (!types.add(s.type())) {
                problems.add("section type " + s.type() + " appears more than once");
            }
            if (!allowed.contains(s.type())) {
                problems.add("section type " + s.type() + " is not available in template " + config.templateId());
            }
            if (requiresHeading(s.type()) && isBlank(s.heading())) {
                problems.add("section '" + s.id() + "' needs a heading");
            }
            if ((s.type() == SectionType.FAQ || s.type() == SectionType.HIGHLIGHTS || s.type() == SectionType.PROCESS)
                    && s.items().isEmpty()) {
                problems.add("section '" + s.id() + "' needs at least one item");
            }
        }
        if (config.sections().getFirst().type() != SectionType.HERO) {
            problems.add("the first section must be HERO");
        }
        if (!types.contains(SectionType.CONTACT)) {
            problems.add("a CONTACT section is required so customers can send inquiries");
        }
        if (contrastWithWhite(config.theme().primaryColor()) < MIN_CONTRAST_WITH_WHITE) {
            problems.add("theme.primaryColor " + config.theme().primaryColor()
                    + " is too light for white button text (needs contrast of at least 4.5:1)");
        }
        return problems;
    }

    private static boolean requiresHeading(SectionType type) {
        return type != SectionType.CTA_BANNER;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** WCAG 2.x contrast ratio between a #RRGGBB colour and white. */
    static double contrastWithWhite(String hex) {
        double l = relativeLuminance(hex);
        return 1.05 / (l + 0.05);
    }

    private static double relativeLuminance(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return 0.2126 * channel((rgb >> 16) & 0xff) + 0.7152 * channel((rgb >> 8) & 0xff) + 0.0722 * channel(rgb & 0xff);
    }

    private static double channel(int c) {
        double s = c / 255.0;
        return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
    }
}
