package com.launchmate.ai.provider;

/** Result returned to the model for one {@link ToolCall}; {@code content} is JSON text. */
public record ToolResult(String toolCallId, String content, boolean isError) {
}
