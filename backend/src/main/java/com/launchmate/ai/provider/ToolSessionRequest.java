package com.launchmate.ai.provider;

import java.util.List;

/**
 * @param system  frozen for the lifetime of the session
 * @param history earlier conversation as plain text turns (no provider-specific state)
 */
public record ToolSessionRequest(String system, List<ToolDefinition> tools, List<ChatTurn> history,
                                 String userMessage, ReasoningEffort effort, long maxTokens) {

    public ToolSessionRequest {
        tools = List.copyOf(tools);
        history = List.copyOf(history);
    }
}
