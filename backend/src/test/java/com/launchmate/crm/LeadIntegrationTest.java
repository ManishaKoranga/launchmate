package com.launchmate.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeadIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    ApiClient owner;
    ApiClient stranger;
    UUID businessId;
    String slug;

    @BeforeEach
    void setUp() throws Exception {
        owner = new ApiClient(mvc, json).registerNewUser();
        stranger = new ApiClient(mvc, json).registerNewUser();
        businessId = owner.createBusiness("Lead Bakery");
        slug = owner.readJson(owner.get("/api/businesses/" + businessId)).get("slug").asText();
    }

    @Test
    void publishedSiteAcceptsInquiriesThatReachOnlyTheOwner() throws Exception {
        String productId = owner.readJson(owner.post("/api/businesses/" + businessId + "/products",
                Map.of("kind", "PRODUCT", "name", "Chocolate truffle cake"))).get("id").asText();
        Map<String, Object> inquiry = Map.of("name", "Riya", "phone", "+91 90000 11111",
                "message", "Need a 1kg cake for Saturday", "productId", productId);

        // Unpublished sites don't accept inquiries
        submit(inquiry).andExpect(status().isNotFound());

        publish();
        submit(inquiry).andExpect(status().isCreated()).andExpect(jsonPath("$.received").value(true));

        String leadId = owner.readJson(owner.get("/api/businesses/" + businessId + "/leads")
                        .andExpect(jsonPath("$.totalItems").value(1))
                        .andExpect(jsonPath("$.items[0].customerName").value("Riya"))
                        .andExpect(jsonPath("$.items[0].productName").value("Chocolate truffle cake"))
                        .andExpect(jsonPath("$.items[0].status").value("NEW"))
                        .andExpect(jsonPath("$.items[0].source").value("WEBSITE")))
                .get("items").get(0).get("id").asText();

        owner.patch("/api/businesses/" + businessId + "/leads/" + leadId + "/status", Map.of("status", "QUALIFIED"))
                .andExpect(jsonPath("$.status").value("QUALIFIED"));
        owner.get("/api/businesses/" + businessId + "/leads/summary")
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.byStatus.QUALIFIED").value(1));
        owner.get("/api/businesses/" + businessId + "/leads?status=NEW").andExpect(jsonPath("$.totalItems").value(0));

        // Cross-tenant
        stranger.get("/api/businesses/" + businessId + "/leads").andExpect(status().isNotFound());
        stranger.get("/api/businesses/" + businessId + "/leads/" + leadId).andExpect(status().isNotFound());
        UUID strangerBusiness = stranger.createBusiness("Other");
        stranger.get("/api/businesses/" + strangerBusiness + "/leads/" + leadId).andExpect(status().isNotFound());
        stranger.patch("/api/businesses/" + strangerBusiness + "/leads/" + leadId + "/status", Map.of("status", "LOST"))
                .andExpect(status().isNotFound());
    }

    @Test
    void productFromAnotherBusinessIsIgnored() throws Exception {
        publish();
        UUID other = stranger.createBusiness("Other shop");
        String foreignProduct = stranger.readJson(stranger.post("/api/businesses/" + other + "/products",
                Map.of("kind", "PRODUCT", "name", "Secret item"))).get("id").asText();
        submit(Map.of("name", "A", "email", "a@example.com", "message", "Hi", "productId", foreignProduct))
                .andExpect(status().isCreated());
        owner.get("/api/businesses/" + businessId + "/leads").andExpect(jsonPath("$.items[0].productName").doesNotExist());
    }

    @Test
    void honeypotAndValidation() throws Exception {
        publish();
        submit(Map.of("name", "Bot", "phone", "+91 90000 11111", "message", "spam", "website", "http://spam"))
                .andExpect(status().isCreated());
        owner.get("/api/businesses/" + businessId + "/leads").andExpect(jsonPath("$.totalItems").value(0));

        submit(Map.of("name", "No contact", "message", "Hello")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contactable").exists());
        submit(Map.of("name", "", "phone", "abc", "message", "")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    private void publish() throws Exception {
        String base = "/api/businesses/" + businessId;
        String job = owner.readJson(owner.post(base + "/website/generation-jobs", Map.of("mode", "TEMPLATE"))).get("id").asText();
        for (int i = 0; i < 100; i++) {
            if (owner.readJson(owner.get(base + "/website/generation-jobs/" + job)).get("status").asText().equals("SUCCEEDED")) break;
            Thread.sleep(50);
        }
        owner.post(base + "/website/publish", Map.of()).andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions submit(Map<String, Object> body) throws Exception {
        // Anonymous visitor: no session, no CSRF token
        return mvc.perform(post("/api/public/sites/" + slug + "/inquiries")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }
}
