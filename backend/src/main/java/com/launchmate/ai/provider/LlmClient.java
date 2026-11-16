package com.launchmate.ai.provider;

/**
 * Port to a large language model. Business modules never see vendor SDK types; swapping Claude for
 * another provider means writing one new adapter.
 */
public interface LlmClient {

    /** Single call whose response is constrained to, and parsed into, {@code request.outputType()}. */
    <T> T structured(StructuredRequest<T> request);

    /**
     * Starts a tool-using exchange for one user message. The caller drives the loop: call
     * {@link ToolSession#next()}, execute requested tools itself, then submit results. The session
     * keeps the provider's raw messages internally so model state is passed back unchanged.
     */
    ToolSession startToolSession(ToolSessionRequest request);

    boolean isConfigured();
}
