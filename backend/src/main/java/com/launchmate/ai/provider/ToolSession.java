package com.launchmate.ai.provider;

import java.util.List;

public interface ToolSession {

    /** Calls the model with everything so far. */
    ModelStep next();

    /** Supplies results for every tool call of the last step, in one batch. */
    void submitToolResults(List<ToolResult> results);
}
