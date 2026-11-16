package com.launchmate.catalogue.controller;

import com.launchmate.shared.storage.ObjectStorage;
import com.launchmate.shared.storage.StorageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/** Serves uploaded images. Keys are random and immutable, so responses are cached aggressively. */
@RestController
@Tag(name = "Public website")
public class PublicAssetController {

    private static final String PREFIX = "/api/public/assets/";
    private final ObjectStorage storage;

    public PublicAssetController(ObjectStorage storage) {
        this.storage = storage;
    }

    @Operation(summary = "Uploaded image")
    @GetMapping(PREFIX + "**")
    public ResponseEntity<byte[]> asset(HttpServletRequest request) {
        String key = request.getRequestURI().substring(PREFIX.length());
        if (!StorageKeys.isValid(key)) {
            return ResponseEntity.notFound().build();
        }
        return storage.get(key)
                .map(o -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(o.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(o.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
