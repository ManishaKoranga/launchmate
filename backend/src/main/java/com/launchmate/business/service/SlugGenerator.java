package com.launchmate.business.service;

import com.launchmate.business.repository.BusinessRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/** Produces a unique, URL-safe slug for a business's LaunchMate URL (/s/{slug}). */
@Component
public class SlugGenerator {

    private static final int MAX_LENGTH = 48;
    private static final Set<String> RESERVED = Set.of("api", "admin", "app", "dashboard", "login", "signup", "www",
            "s", "launchmate", "static", "assets", "help", "support");
    private static final String ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789";

    private final BusinessRepository businesses;
    private final SecureRandom random = new SecureRandom();

    public SlugGenerator(BusinessRepository businesses) {
        this.businesses = businesses;
    }

    public String generate(String businessName, String city) {
        String base = slugify(city == null ? businessName : businessName + " " + city);
        if (base.length() < 3 || RESERVED.contains(base)) {
            base = (base + "-site").replaceAll("^-", "");
        }
        String candidate = base;
        for (int attempt = 0; businesses.existsBySlug(candidate); attempt++) {
            if (attempt > 20) {
                throw new IllegalStateException("Could not allocate a unique slug");
            }
            candidate = truncate(base, MAX_LENGTH - 5) + "-" + randomSuffix();
        }
        return candidate;
    }

    static String slugify(String input) {
        String ascii = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = ascii.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return truncate(slug, MAX_LENGTH).replaceAll("-+$", "");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
