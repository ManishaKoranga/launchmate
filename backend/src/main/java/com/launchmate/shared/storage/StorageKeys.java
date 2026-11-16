package com.launchmate.shared.storage;

import java.util.regex.Pattern;

public final class StorageKeys {

    /** e.g. products/{uuid}/{uuid}.webp — no "..", no leading slash, limited alphabet. */
    private static final Pattern VALID = Pattern.compile("^[a-z]+(/[0-9a-f-]{36}){2}\\.(jpg|png|webp)$");

    private StorageKeys() {
    }

    public static boolean isValid(String key) {
        return key != null && VALID.matcher(key).matches();
    }
}
