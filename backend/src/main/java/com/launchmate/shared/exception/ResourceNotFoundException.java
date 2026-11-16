package com.launchmate.shared.exception;

/** Thrown when a resource does not exist <em>or</em> is outside the caller's tenant. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource) {
        super(resource + " not found");
    }
}
