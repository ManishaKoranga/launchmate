package com.launchmate.shared.storage;

import com.launchmate.shared.exception.BusinessRuleException;

import java.util.Arrays;

/** Accepts only JPEG, PNG and WebP, identified by file signature rather than the client's claim. */
public final class ImageValidator {

    public static final int MAX_BYTES = 5 * 1024 * 1024;

    public record ImageType(String extension, String contentType) {
    }

    private ImageValidator() {
    }

    public static ImageType detect(byte[] data) {
        if (data == null || data.length == 0) {
            throw new BusinessRuleException("The file is empty");
        }
        if (data.length > MAX_BYTES) {
            throw new BusinessRuleException("Images must be 5 MB or smaller");
        }
        if (startsWith(data, 0xFF, 0xD8, 0xFF)) {
            return new ImageType("jpg", "image/jpeg");
        }
        if (startsWith(data, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return new ImageType("png", "image/png");
        }
        if (data.length > 12 && Arrays.equals(Arrays.copyOfRange(data, 0, 4), "RIFF".getBytes())
                && Arrays.equals(Arrays.copyOfRange(data, 8, 12), "WEBP".getBytes())) {
            return new ImageType("webp", "image/webp");
        }
        throw new BusinessRuleException("Please upload a JPEG, PNG or WebP image");
    }

    private static boolean startsWith(byte[] data, int... sig) {
        if (data.length < sig.length) {
            return false;
        }
        for (int i = 0; i < sig.length; i++) {
            if ((data[i] & 0xFF) != sig[i]) {
                return false;
            }
        }
        return true;
    }
}
