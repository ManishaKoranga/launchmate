package com.launchmate.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.catalogue.domain.ProductInput;
import com.launchmate.catalogue.domain.ProductKind;
import com.launchmate.onboarding.service.BusinessProfileExtractor;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OnboardingIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void conversationExtractsProfileAndCompletionCreatesBusinessWithProducts() throws Exception {
        ApiClient owner = new ApiClient(mvc, json).registerNewUser();
        String id = owner.readJson(owner.post("/api/onboarding/conversations", Map.of()).andExpect(status().isCreated()))
                .get("id").asText();

        // First turn: no name or phone yet
        when(profileExtractor.extract(any(), anyList(), anyList(), anyString())).thenReturn(
                new BusinessProfileExtractor.Extraction(
                        new BusinessProfile(null, BusinessCategory.BAKERY_FOOD, null, "Home bakery", "Deoband",
                                null, null, null, null, null, "Local families", null, null),
                        List.of(new ProductInput(ProductKind.PRODUCT, "Custom cakes", null, null, null, null, true)),
                        "Bahut badhiya! Aapki bakery ka naam kya hai?"));
        owner.post("/api/onboarding/conversations/" + id + "/messages",
                        Map.of("text", "Main Deoband mein home bakery start karna chahti hoon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readyForReview").value(false))
                .andExpect(jsonPath("$.missingFields[0]").value("name"))
                .andExpect(jsonPath("$.messages.length()").value(2));

        // Second turn: model "forgets" the city — the server keeps it
        when(profileExtractor.extract(any(), anyList(), anyList(), anyString())).thenReturn(
                new BusinessProfileExtractor.Extraction(
                        new BusinessProfile("Meera's Bakes", BusinessCategory.BAKERY_FOOD, null, null, null,
                                null, null, "+91 98765 43210", null, null, null, null, null),
                        List.of(new ProductInput(ProductKind.PRODUCT, "Custom cakes", null, new BigDecimal("650"), "per kg", null, true)),
                        "Perfect! Ab aap details review kar sakti hain."));
        owner.post("/api/onboarding/conversations/" + id + "/messages", Map.of("text", "Meera's Bakes, 98765 43210"))
                .andExpect(jsonPath("$.readyForReview").value(true))
                .andExpect(jsonPath("$.profile.city").value("Deoband"))
                .andExpect(jsonPath("$.profile.description").value("Home bakery"));

        // Resume works
        owner.get("/api/onboarding/conversations/latest").andExpect(jsonPath("$.id").value(id));

        JsonNode business = owner.readJson(owner.post("/api/onboarding/conversations/" + id + "/complete", Map.of(
                        "profile", Map.of("name", "Meera's Bakes", "category", "BAKERY_FOOD", "city", "Deoband",
                                "phone", "+91 98765 43210"),
                        "offerings", List.of(Map.of("kind", "PRODUCT", "name", "Custom cakes", "price", 650, "priceNote", "per kg"))))
                .andExpect(status().isCreated()));
        String businessId = business.get("id").asText();
        owner.get("/api/businesses/" + businessId).andExpect(jsonPath("$.setupStage").value("PROFILE_CONFIRMED"));
        owner.get("/api/businesses/" + businessId + "/products")
                .andExpect(jsonPath("$.items[0].name").value("Custom cakes"))
                .andExpect(jsonPath("$.items[0].price").value(650));

        owner.post("/api/onboarding/conversations/" + id + "/complete", Map.of(
                        "profile", Map.of("name", "Again", "category", "BAKERY_FOOD"), "offerings", List.of()))
                .andExpect(status().isConflict());
        owner.get("/api/onboarding/conversations/latest").andExpect(status().isNotFound());
    }

    @Test
    void otherUsersCannotAccessAConversation() throws Exception {
        ApiClient owner = new ApiClient(mvc, json).registerNewUser();
        ApiClient stranger = new ApiClient(mvc, json).registerNewUser();
        String id = owner.readJson(owner.post("/api/onboarding/conversations", Map.of())).get("id").asText();
        stranger.get("/api/onboarding/conversations/" + id).andExpect(status().isNotFound());
        stranger.post("/api/onboarding/conversations/" + id + "/messages", Map.of("text", "hi")).andExpect(status().isNotFound());
        stranger.post("/api/onboarding/conversations/" + id + "/complete", Map.of(
                "profile", Map.of("name", "X", "category", "BAKERY_FOOD"), "offerings", List.of()))
                .andExpect(status().isNotFound());
    }

    @Test
    void completionValidatesTheApprovedProfile() throws Exception {
        ApiClient owner = new ApiClient(mvc, json).registerNewUser();
        String id = owner.readJson(owner.post("/api/onboarding/conversations", Map.of())).get("id").asText();
        owner.post("/api/onboarding/conversations/" + id + "/complete", Map.of(
                        "profile", Map.of("name", "", "category", "BAKERY_FOOD", "phone", "call me"), "offerings", List.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['profile.name']").exists())
                .andExpect(jsonPath("$.errors['profile.phone']").exists());
    }
}
