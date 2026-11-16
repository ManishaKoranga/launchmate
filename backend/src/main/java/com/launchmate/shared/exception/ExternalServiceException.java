package com.launchmate.shared.exception;

/**
 * An external dependency (LLM, registrar, storage, speech-to-text) is not configured or failed.
 * The message is safe to show to users; never put provider secrets or raw payloads in it.
 */
public class ExternalServiceException extends RuntimeException {
    private final boolean notConfigured;

    public ExternalServiceException(String message, boolean notConfigured, Throwable cause) {
        super(message, cause);
        this.notConfigured = notConfigured;
    }

    public static ExternalServiceException notConfigured(String service) {
        return new ExternalServiceException(service + " is not configured on this server", true, null);
    }

    public static ExternalServiceException failed(String service, Throwable cause) {
        return new ExternalServiceException(service + " is temporarily unavailable. Please try again.", false, cause);
    }

    public boolean isNotConfigured() {
        return notConfigured;
    }
}
