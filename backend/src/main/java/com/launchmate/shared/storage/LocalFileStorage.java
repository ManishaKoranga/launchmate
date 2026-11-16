package com.launchmate.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Stores objects under a base directory. Keys are validated so they can never escape it. */
@Component
public class LocalFileStorage implements ObjectStorage {

    private final Path root;

    public LocalFileStorage(@Value("${launchmate.storage.local-path:./uploads}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, byte[] content, String contentType) {
        Path file = resolve(key);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, content);
            Files.writeString(file.resolveSibling(file.getFileName() + ".type"), contentType);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store object", e);
        }
    }

    @Override
    public Optional<StoredObject> get(String key) {
        Path file = resolve(key);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            Path typeFile = file.resolveSibling(file.getFileName() + ".type");
            String type = Files.exists(typeFile) ? Files.readString(typeFile) : "application/octet-stream";
            return Optional.of(new StoredObject(Files.readAllBytes(file), type));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read object", e);
        }
    }

    @Override
    public void delete(String key) {
        Path file = resolve(key);
        try {
            Files.deleteIfExists(file);
            Files.deleteIfExists(file.resolveSibling(file.getFileName() + ".type"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete object", e);
        }
    }

    private Path resolve(String key) {
        if (!StorageKeys.isValid(key)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        Path p = root.resolve(key).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return p;
    }
}
