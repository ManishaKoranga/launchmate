package com.launchmate.ai.provider;

import java.util.List;

/**
 * @param conversation alternating turns, starting with a USER turn
 */
public record StructuredRequest<T>(String system, List<ChatTurn> conversation, Class<T> outputType,
                                   ReasoningEffort effort, long maxTokens) {

    public StructuredRequest {
        if (conversation.isEmpty() || conversation.getFirst().role() != ChatTurn.Role.USER) {
            throw new IllegalArgumentException("conversation must start with a user turn");
        }
        conversation = List.copyOf(conversation);
    }
}
