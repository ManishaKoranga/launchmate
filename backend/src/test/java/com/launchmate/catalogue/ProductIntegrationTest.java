package com.launchmate.catalogue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void crudAndTenantIsolation() throws Exception {
        ApiClient owner = new ApiClient(mvc, json).registerNewUser();
        ApiClient stranger = new ApiClient(mvc, json).registerNewUser();
        UUID business = owner.createBusiness("Cake Corner");
        UUID strangerBusiness = stranger.createBusiness("Other");
        String base = "/api/businesses/" + business + "/products";

        String id = owner.readJson(owner.post(base, Map.of("kind", "PRODUCT", "name", "Red velvet cake", "price", 650.5))
                .andExpect(status().isCreated())).get("id").asText();
        owner.put(base + "/" + id, Map.of("kind", "PRODUCT", "name", "Red velvet cake (1 kg)", "price", 700, "visible", false))
                .andExpect(status().isOk()).andExpect(jsonPath("$.visible").value(false));
        owner.get(base).andExpect(jsonPath("$.totalItems").value(1));

        stranger.get(base + "/" + id).andExpect(status().isNotFound());
        stranger.put(base + "/" + id, Map.of("kind", "PRODUCT", "name", "Hacked")).andExpect(status().isNotFound());
        stranger.delete(base + "/" + id).andExpect(status().isNotFound());
        // Using their own business id with someone else's product id also fails
        stranger.get("/api/businesses/" + strangerBusiness + "/products/" + id).andExpect(status().isNotFound());
        stranger.delete("/api/businesses/" + strangerBusiness + "/products/" + id).andExpect(status().isNotFound());

        owner.delete(base + "/" + id).andExpect(status().isNoContent());
        owner.get(base).andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void imageUploadIsValidatedTenantScopedAndServedPublicly() throws Exception {
        ApiClient owner = new ApiClient(mvc, json).registerNewUser();
        ApiClient stranger = new ApiClient(mvc, json).registerNewUser();
        String base = "/api/businesses/" + owner.createBusiness("Pics") + "/products";
        String id = owner.readJson(owner.post(base, Map.of("kind", "PRODUCT", "name", "Cake"))).get("id").asText();

        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        var good = new org.springframework.mock.web.MockMultipartFile("file", "cake.png", "image/png", png);
        var fake = new org.springframework.mock.web.MockMultipartFile("file", "evil.png", "image/png", "<script>".getBytes());

        String imageUrl = owner.readJson(owner.multipart(base + "/" + id + "/image", good).andExpect(status().isOk()))
                .get("imageUrl").asText();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(imageUrl))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType("image/png"));

        owner.multipart(base + "/" + id + "/image", fake).andExpect(status().isUnprocessableEntity());
        stranger.multipart(base + "/" + id + "/image", good).andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/public/assets/../../etc/passwd"))
                .andExpect(status().is4xxClientError()); // rejected by the firewall (400) or key validation (404)
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/public/assets/products/x.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationAndPlanLimit() throws Exception {
        ApiClient owner = new ApiClient(mvc, json).registerNewUser();
        String base = "/api/businesses/" + owner.createBusiness("Limits") + "/products";
        owner.post(base, Map.of("kind", "PRODUCT", "name", "", "price", -5)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists()).andExpect(jsonPath("$.errors.price").exists());
        for (int i = 0; i < 20; i++) {
            owner.post(base, Map.of("kind", "SERVICE", "name", "Item " + i)).andExpect(status().isCreated());
        }
        owner.post(base, Map.of("kind", "SERVICE", "name", "One too many")).andExpect(status().isUnprocessableEntity());
    }
}
