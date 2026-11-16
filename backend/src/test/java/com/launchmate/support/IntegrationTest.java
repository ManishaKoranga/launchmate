package com.launchmate.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for integration tests against a real PostgreSQL (never H2 — JSONB and constraints matter).
 * <ul>
 *   <li>If {@code LM_TEST_DB_URL} is set, that database is used (CI without Docker / local dev).</li>
 *   <li>Otherwise a Testcontainers PostgreSQL 16 is started (requires Docker).</li>
 * </ul>
 * Tests create their own users/businesses with random identifiers, so they don't interfere.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    private static PostgreSQLContainer<?> container;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        String url = System.getenv("LM_TEST_DB_URL");
        if (url != null && !url.isBlank()) {
            registry.add("spring.datasource.url", () -> url);
            registry.add("spring.datasource.username", () -> env("LM_TEST_DB_USERNAME", "launchmate"));
            registry.add("spring.datasource.password", () -> env("LM_TEST_DB_PASSWORD", "launchmate"));
            return;
        }
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new IllegalStateException(
                    "Integration tests need PostgreSQL: start Docker (Testcontainers) or set LM_TEST_DB_URL");
        }
        synchronized (IntegrationTest.class) {
            if (container == null) {
                container = new PostgreSQLContainer<>("postgres:16-alpine");
                container.start();
            }
        }
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null ? fallback : v;
    }
}
