package com.launchmate.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;

/** A tool invocation requested by the model. Arguments are untrusted until validated. */
public record ToolCall(String id, String name, JsonNode arguments) {
}
