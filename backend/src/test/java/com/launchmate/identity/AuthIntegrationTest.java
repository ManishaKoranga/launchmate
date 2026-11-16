package com.launchmate.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void registerLogsInAndMeReturnsUser() throws Exception {
        ApiClient client = new ApiClient(mvc, json).registerNewUser();
        client.get("/api/auth/me").andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void unauthenticatedRequestsGet401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/businesses")).andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsRejectedCaseInsensitively() throws Exception {
        String email = "Dup-" + UUID.randomUUID() + "@Example.com";
        new ApiClient(mvc, json).post("/api/auth/register", Map.of("email", email, "password", "password123", "fullName", "A"))
                .andExpect(status().isCreated());
        new ApiClient(mvc, json).post("/api/auth/register", Map.of("email", email.toLowerCase(), "password", "password123", "fullName", "B"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@example.com";
        new ApiClient(mvc, json).post("/api/auth/register", Map.of("email", email, "password", "password123", "fullName", "A"))
                .andExpect(status().isCreated());
        new ApiClient(mvc, json).post("/api/auth/login", Map.of("email", email, "password", "wrong-password"))
                .andExpect(status().isUnauthorized());
        new ApiClient(mvc, json).post("/api/auth/login", Map.of("email", email.toUpperCase(), "password", "password123"))
                .andExpect(status().isOk());
    }

    @Test
    void stateChangingRequestWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@example.com\",\"password\":\"password123\",\"fullName\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsAreReportedPerField() throws Exception {
        new ApiClient(mvc, json).post("/api/auth/register", Map.of("email", "not-an-email", "password", "short", "fullName", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }
}
