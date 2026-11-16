package com.launchmate.shared.storage;

import java.util.Optional;

/**
 * Port for binary assets (product images). Large files never go into PostgreSQL; only keys do.
 * Implementations: local filesystem (development) — an S3-compatible adapter can be added without
 * touching callers.
 */
public interface ObjectStorage {

    void put(String key, byte[] content, String contentType);

    Optional<StoredObject> get(String key);

    void delete(String key);

    record StoredObject(byte[] content, String contentType) {
    }
}
