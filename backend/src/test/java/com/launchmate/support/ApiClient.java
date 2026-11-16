package com.launchmate.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test helper that drives the API as a real browser session would (session cookie + CSRF). */
public final class ApiClient {

    private final MockMvc mvc;
    private final ObjectMapper json;
    private final MockHttpSession session = new MockHttpSession();

    public ApiClient(MockMvc mvc, ObjectMapper json) {
        this.mvc = mvc;
        this.json = json;
    }

    /** Registers a fresh user (random email) and keeps the authenticated session. */
    public ApiClient registerNewUser() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        post("/api/auth/register", Map.of("email", email, "password", "s3cret-pass", "fullName", "Test User"))
                .andExpect(status().isCreated());
        return this;
    }

    public UUID createBusiness(String name) throws Exception {
        String body = post("/api/businesses", Map.of("name", name, "category", "BAKERY_FOOD", "city", "Deoband"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(json.readTree(body).get("id").asText());
    }

    public ResultActions get(String url) throws Exception {
        return mvc.perform(withSession(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url)));
    }

    public ResultActions post(String url, Object body) throws Exception {
        return mvc.perform(withBody(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url), body));
    }

    public ResultActions put(String url, Object body) throws Exception {
        return mvc.perform(withBody(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url), body));
    }

    public ResultActions patch(String url, Object body) throws Exception {
        return mvc.perform(withBody(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(url), body));
    }

    public ResultActions delete(String url) throws Exception {
        return mvc.perform(withSession(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url)).with(csrf()));
    }

    public ResultActions multipart(String url, org.springframework.mock.web.MockMultipartFile file) throws Exception {
        return mvc.perform(withSession(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart(url).file(file)).with(csrf()));
    }

    public JsonNode readJson(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder withSession(MockHttpServletRequestBuilder b) {
        return b.session(session);
    }

    private MockHttpServletRequestBuilder withBody(MockHttpServletRequestBuilder b, Object body) throws Exception {
        return withSession(b).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
    }
}
