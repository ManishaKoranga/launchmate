package com.launchmate.ai.provider;

import java.util.List;

/**
 * One model response inside a tool session.
 *
 * @param text      visible text (may be empty when the model only calls tools)
 * @param toolCalls tools the model wants executed before it continues
 * @param outcome   why the model stopped
 */
public record ModelStep(String text, List<ToolCall> toolCalls, Outcome outcome) {

    public enum Outcome { FINISHED, NEEDS_TOOL_RESULTS, TRUNCATED, REFUSED }
}
