package com.launchmate.ai.provider;

import java.util.Map;

/** A tool the model may request. {@code inputSchema} is a JSON Schema object. */
public record ToolDefinition(String name, String description, Map<String, Object> inputSchema) {
}
