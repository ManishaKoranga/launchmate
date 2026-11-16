package com.launchmate.website;

import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.website.domain.Section;
import com.launchmate.website.domain.SectionType;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.domain.SiteConfigValidator;
import com.launchmate.website.domain.Theme;
import com.launchmate.website.template.StarterSiteFactory;
import com.launchmate.website.template.TemplateRegistry;
import com.launchmate.website.template.WebsiteTemplate;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Every built-in template must produce a valid site with no AI involved. */
class WebsiteTemplatesTest {

    static final TemplateRegistry registry = new TemplateRegistry();
    final SiteConfigValidator validator = new SiteConfigValidator(
            Validation.buildDefaultValidatorFactory().getValidator(), registry);
    final StarterSiteFactory factory = new StarterSiteFactory();

    static List<WebsiteTemplate> templates() {
        return registry.all();
    }

    @ParameterizedTest
    @MethodSource("templates")
    void starterSiteIsValid(WebsiteTemplate t) {
        Business b = new Business("slug", new BusinessProfile("Meera's Home Bakes", t.category(), null, null,
                "Deoband", null, null, "+91 98765 43210", null, null, null, null, null));
        assertThat(validator.validate(factory.create(t, b))).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("templates")
    void retemplatingKeepsSiteValid(WebsiteTemplate t) {
        Business b = new Business("slug", new BusinessProfile("Studio", t.category(), "Tag", "Desc", null, null,
                null, null, null, null, null, null, null));
        SiteConfig bakery = factory.create(registry.require("bakery"), b);
        assertThat(validator.validate(factory.retemplate(bakery, t, b))).isEmpty();
    }

    @Test
    void rejectsMissingContactDuplicateTypesAndLowContrast() {
        Business b = new Business("slug", new BusinessProfile("X", registry.require("bakery").category(), null,
                null, null, null, null, null, null, null, null, null, null));
        SiteConfig ok = factory.create(registry.require("bakery"), b);

        List<Section> noContact = new ArrayList<>(ok.sections());
        noContact.removeIf(s -> s.type() == SectionType.CONTACT);
        noContact.add(1, ok.sections().getFirst()); // duplicate HERO
        Theme pale = new Theme("#FFD1DC", "#FFFFFF", ok.theme().palette(), ok.theme().fontPairing(), ok.theme().corners());

        List<String> problems = validator.validate(new SiteConfig("bakery", pale, ok.seo(), noContact));
        assertThat(problems).anyMatch(p -> p.contains("CONTACT"))
                .anyMatch(p -> p.contains("more than once"))
                .anyMatch(p -> p.contains("primaryColor"));
    }

    @Test
    void rejectsUnknownTemplateAndBadColourFormat() {
        Business b = new Business("slug", new BusinessProfile("X", registry.require("salon").category(), null,
                null, null, null, null, null, null, null, null, null, null));
        SiteConfig ok = factory.create(registry.require("salon"), b);
        assertThat(validator.validate(new SiteConfig("nope", ok.theme(), ok.seo(), ok.sections())))
                .anyMatch(p -> p.contains("not a known template"));
        Theme bad = new Theme("red", "#FFFFFF", ok.theme().palette(), ok.theme().fontPairing(), ok.theme().corners());
        assertThat(validator.validate(new SiteConfig("salon", bad, ok.seo(), ok.sections())))
                .anyMatch(p -> p.contains("primaryColor"));
    }
}
