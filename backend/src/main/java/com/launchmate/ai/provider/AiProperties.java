package com.launchmate.ai.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param enabled  set false to run without any LLM (AI features report "not configured")
 * @param model    Claude model id
 * @param timeout  per-attempt HTTP timeout
 * @param maxRetries retries for 429/5xx/connection errors (SDK applies backoff)
 */
@ConfigurationProperties(prefix = "launchmate.ai")
public record AiProperties(boolean enabled, String model, Duration timeout, int maxRetries) {

    public AiProperties {
        model = model == null || model.isBlank() ? "claude-opus-5-5" : model;
        timeout = timeout == null ? Duration.ofSeconds(120) : timeout;
        maxRetries = maxRetries <= 0 ? 2 : maxRetries;
    }
}
