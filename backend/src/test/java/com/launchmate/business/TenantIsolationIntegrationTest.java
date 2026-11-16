package com.launchmate.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cross-tenant access attempts must be indistinguishable from "not found". */
class TenantIsolationIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    ApiClient alice;
    ApiClient mallory;
    UUID aliceBusiness;

    @BeforeEach
    void setUp() throws Exception {
        alice = new ApiClient(mvc, json).registerNewUser();
        mallory = new ApiClient(mvc, json).registerNewUser();
        aliceBusiness = alice.createBusiness("Alice Cakes");
    }

    @Test
    void ownerCanReadOwnBusiness() throws Exception {
        alice.get("/api/businesses/" + aliceBusiness).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Cakes"))
                .andExpect(jsonPath("$.slug").value(org.hamcrest.Matchers.startsWith("alice-cakes-deoband")));
    }

    @Test
    void otherUserCannotReadBusiness() throws Exception {
        mallory.get("/api/businesses/" + aliceBusiness).andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotUpdateBusiness() throws Exception {
        mallory.put("/api/businesses/" + aliceBusiness, Map.of("name", "Hacked", "category", "BAKERY_FOOD"))
                .andExpect(status().isNotFound());
        alice.get("/api/businesses/" + aliceBusiness).andExpect(jsonPath("$.name").value("Alice Cakes"));
    }

    @Test
    void otherUserDoesNotSeeBusinessInList() throws Exception {
        mallory.get("/api/businesses").andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        alice.get("/api/businesses").andExpect(jsonPath("$[0].id").value(aliceBusiness.toString()));
    }

    @Test
    void malformedAndUnknownIdsAreNotFound() throws Exception {
        alice.get("/api/businesses/not-a-uuid").andExpect(status().isNotFound());
        alice.get("/api/businesses/" + UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    void slugsAreUniquePerBusiness() throws Exception {
        String first = alice.readJson(alice.get("/api/businesses/" + aliceBusiness)).get("slug").asText();
        UUID second = mallory.createBusiness("Alice Cakes");
        String secondSlug = mallory.readJson(mallory.get("/api/businesses/" + second)).get("slug").asText();
        org.assertj.core.api.Assertions.assertThat(secondSlug).startsWith("alice-cakes-deoband").isNotEqualTo(first);
    }

    @Test
    void ownerCanUpdateHoursAndInvalidHoursAreRejected() throws Exception {
        alice.put("/api/businesses/" + aliceBusiness + "/hours", Map.of("openingHours", java.util.List.of(
                        Map.of("day", "MONDAY", "closed", false, "opens", "10:00", "closes", "20:00"),
                        Map.of("day", "SUNDAY", "closed", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openingHours.length()").value(2));

        alice.put("/api/businesses/" + aliceBusiness + "/hours", Map.of("openingHours", java.util.List.of(
                        Map.of("day", "MONDAY", "closed", false, "opens", "20:00", "closes", "10:00"))))
                .andExpect(status().isBadRequest());
    }
}
