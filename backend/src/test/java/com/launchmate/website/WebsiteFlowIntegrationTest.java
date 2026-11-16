package com.launchmate.website;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.launchmate.shared.exception.ExternalServiceException;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.domain.Theme;
import com.launchmate.website.template.StarterSiteFactory;
import com.launchmate.website.template.TemplateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebsiteFlowIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TemplateRegistry templates;
    @Autowired StarterSiteFactory starterFactory;

    ApiClient owner;
    ApiClient stranger;
    UUID businessId;
    String base;

    @BeforeEach
    void setUp() throws Exception {
        owner = new ApiClient(mvc, json).registerNewUser();
        stranger = new ApiClient(mvc, json).registerNewUser();
        businessId = owner.createBusiness("Sweet Home");
        base = "/api/businesses/" + businessId;
    }

    @Test
    void templateGenerationPublishAndDraftIsolation() throws Exception {
        JsonNode job = generate("TEMPLATE");
        assertThat(job.get("status").asText()).isEqualTo("SUCCEEDED");
        assertThat(job.get("steps")).allMatch(s -> s.get("status").asText().equals("DONE"));

        JsonNode website = owner.readJson(owner.get(base + "/website").andExpect(status().isOk()));
        assertThat(website.get("draftSource").asText()).isEqualTo("TEMPLATE_STARTER");
        assertThat(website.get("draft").get("sections").get(0).get("type").asText()).isEqualTo("HERO");
        assertThat(website.has("published")).isFalse();
        String slug = owner.readJson(owner.get(base)).get("slug").asText();

        // Not public until published
        mvc.perform(get("/api/public/sites/" + slug)).andExpect(status().isNotFound());

        owner.post(base + "/website/publish", Map.of()).andExpect(status().isOk())
                .andExpect(jsonPath("$.published.version").value(1))
                .andExpect(jsonPath("$.hasUnpublishedChanges").value(false));
        mvc.perform(get("/api/public/sites/" + slug)).andExpect(status().isOk())
                .andExpect(jsonPath("$.config.sections[0].heading").value("Sweet Home"))
                .andExpect(jsonPath("$.business.name").value("Sweet Home"));

        // Editing the draft must not change the published site
        ObjectNode draft = (ObjectNode) website.get("draft").deepCopy();
        ((ObjectNode) draft.get("sections").get(0)).put("heading", "Fresh cakes daily");
        owner.put(base + "/website/draft", Map.of("config", draft, "expectedRevision", website.get("draftRevision").asInt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasUnpublishedChanges").value(true));
        mvc.perform(get("/api/public/sites/" + slug))
                .andExpect(jsonPath("$.config.sections[0].heading").value("Sweet Home"));
        owner.get(base + "/website/preview").andExpect(jsonPath("$.config.sections[0].heading").value("Fresh cakes daily"))
                .andExpect(jsonPath("$.preview").value(true));

        // Stale revision is rejected
        owner.put(base + "/website/draft", Map.of("config", draft, "expectedRevision", website.get("draftRevision").asInt()))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidDraftIsRejectedWithProblems() throws Exception {
        generate("TEMPLATE");
        JsonNode website = owner.readJson(owner.get(base + "/website"));
        ObjectNode draft = (ObjectNode) website.get("draft").deepCopy();
        ArrayNode sections = (ArrayNode) draft.get("sections");
        for (int i = sections.size() - 1; i >= 0; i--) {
            if (sections.get(i).get("type").asText().equals("CONTACT")) sections.remove(i);
        }
        ((ObjectNode) draft.get("theme")).put("primaryColor", "#FFEEEE");
        owner.put(base + "/website/draft", Map.of("config", draft, "expectedRevision", website.get("draftRevision").asInt()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.problems").isArray());
    }

    @Test
    void strangerCannotTouchAnotherTenantsWebsite() throws Exception {
        JsonNode job = generate("TEMPLATE");
        JsonNode website = owner.readJson(owner.get(base + "/website"));
        stranger.get(base + "/website").andExpect(status().isNotFound());
        stranger.get(base + "/website/preview").andExpect(status().isNotFound());
        stranger.get(base + "/website/generation-jobs/" + job.get("id").asText()).andExpect(status().isNotFound());
        stranger.put(base + "/website/draft", Map.of("config", website.get("draft"), "expectedRevision", 1))
                .andExpect(status().isNotFound());
        stranger.post(base + "/website/publish", Map.of()).andExpect(status().isNotFound());
        stranger.post(base + "/website/generation-jobs", Map.of("mode", "TEMPLATE")).andExpect(status().isNotFound());

        // A stranger's own business cannot be used to read someone else's job either
        UUID strangerBusiness = stranger.createBusiness("Other");
        stranger.get("/api/businesses/" + strangerBusiness + "/website/generation-jobs/" + job.get("id").asText())
                .andExpect(status().isNotFound());
    }

    @Test
    void aiGenerationRetriesOnceWithValidationFeedback() throws Exception {
        var template = templates.require("bakery");
        var valid = starterFactory.create(template, new com.launchmate.business.domain.Business("x",
                new com.launchmate.business.domain.BusinessProfile("Sweet Home",
                        com.launchmate.business.domain.BusinessCategory.BAKERY_FOOD, null, null, null, null, null,
                        null, null, null, null, null, null)));
        var invalid = new SiteConfig(valid.templateId(), new Theme("#FFFFFF", "#FFFFFF", valid.theme().palette(),
                valid.theme().fontPairing(), valid.theme().corners()), valid.seo(), valid.sections());
        when(websiteGenerator.generate(any(), any(), anyList(), any(), anyList())).thenReturn(invalid, valid);

        JsonNode job = generate("AI");
        assertThat(job.get("status").asText()).isEqualTo("SUCCEEDED");
        owner.get(base + "/website").andExpect(jsonPath("$.draftSource").value("AI_GENERATED"));
    }

    @Test
    void aiGenerationFailsClearlyWhenAiIsNotConfigured() throws Exception {
        when(websiteGenerator.generate(any(), any(), anyList(), any(), anyList()))
                .thenThrow(ExternalServiceException.notConfigured("AI"));
        JsonNode job = generate("AI");
        assertThat(job.get("status").asText()).isEqualTo("FAILED");
        assertThat(job.get("errorMessage").asText()).contains("template content");
        owner.get(base + "/website").andExpect(status().isNotFound());
    }

    private JsonNode generate(String mode) throws Exception {
        JsonNode job = owner.readJson(owner.post(base + "/website/generation-jobs", Map.of("mode", mode))
                .andExpect(status().isAccepted()));
        String id = job.get("id").asText();
        for (int i = 0; i < 100; i++) {
            job = owner.readJson(owner.get(base + "/website/generation-jobs/" + id));
            String s = job.get("status").asText();
            if (s.equals("SUCCEEDED") || s.equals("FAILED")) {
                return job;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("generation did not finish: " + job);
    }
}
