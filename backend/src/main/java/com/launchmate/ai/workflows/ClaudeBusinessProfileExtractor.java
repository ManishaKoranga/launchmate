package com.launchmate.ai.workflows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.ai.prompts.OnboardingPrompts;
import com.launchmate.ai.provider.ChatTurn;
import com.launchmate.ai.provider.LlmClient;
import com.launchmate.ai.provider.ReasoningEffort;
import com.launchmate.ai.provider.StructuredRequest;
import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.catalogue.domain.ProductInput;
import com.launchmate.catalogue.domain.ProductKind;
import com.launchmate.onboarding.service.BusinessProfileExtractor;
import com.launchmate.onboarding.service.OnboardingTurn;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Business-understanding workflow: free text (incl. Hinglish) → structured profile + next question. */
@Component
class ClaudeBusinessProfileExtractor implements BusinessProfileExtractor {

    private static final int MAX_OFFERINGS = 20;

    private final LlmClient llm;
    private final ObjectMapper json;

    ClaudeBusinessProfileExtractor(LlmClient llm, ObjectMapper json) {
        this.llm = llm;
        this.json = json;
    }

    @Override
    public Extraction extract(BusinessProfile currentDraft, List<ProductInput> currentOfferings,
                              List<OnboardingTurn> history, String newMessage) {
        List<ChatTurn> turns = new ArrayList<>();
        for (OnboardingTurn t : history) {
            turns.add(t.fromOwner() ? ChatTurn.user(t.text()) : ChatTurn.assistant(t.text()));
        }
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("profile", currentDraft);
        draft.put("offerings", currentOfferings);
        turns.add(ChatTurn.user("""
                <current_draft>
                %s
                </current_draft>

                <owner_message>
                %s
                </owner_message>
                """.formatted(toJson(draft), newMessage)));

        OnboardingExtraction out = llm.structured(new StructuredRequest<>(OnboardingPrompts.SYSTEM, turns,
                OnboardingExtraction.class, ReasoningEffort.LOW, 8000));
        return new Extraction(toProfile(out.profile()), toOfferings(out.offerings()), out.reply().trim());
    }

    private static BusinessProfile toProfile(OnboardingExtraction.Profile p) {
        if (p == null) {
            return null;
        }
        return new BusinessProfile(clean(p.name(), 120), category(p.category()), clean(p.tagline(), 200),
                clean(p.description(), 4000), clean(p.city(), 80), clean(p.area(), 120), null,
                phone(p.phone()), phone(p.whatsapp()), clean(p.email(), 254), clean(p.targetCustomers(), 300),
                clean(p.languages(), 120), null);
    }

    private static List<ProductInput> toOfferings(List<OnboardingExtraction.Offering> offerings) {
        if (offerings == null) {
            return List.of();
        }
        return offerings.stream()
                .filter(Objects::nonNull)
                .filter(o -> o.name() != null && !o.name().isBlank())
                .limit(MAX_OFFERINGS)
                .map(o -> new ProductInput(
                        "SERVICE".equalsIgnoreCase(o.kind()) ? ProductKind.SERVICE : ProductKind.PRODUCT,
                        clean(o.name(), 120),
                        clean(o.description(), 1000),
                        o.priceInRupees() > 0 && o.priceInRupees() < 10_000_000 ? BigDecimal.valueOf(o.priceInRupees()) : null,
                        clean(o.priceNote(), 60),
                        null,
                        true))
                .toList();
    }

    private static BusinessCategory category(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return BusinessCategory.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Keeps only characters valid for the phone format; invalid results are dropped rather than stored. */
    private static String phone(String raw) {
        String cleaned = clean(raw, 30);
        if (cleaned == null) {
            return null;
        }
        String normalised = cleaned.replaceAll("[^+0-9 ()-]", "").trim();
        return normalised.matches("^[+0-9 ()-]{6,20}$") ? normalised : null;
    }

    private static String clean(String s, int max) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private String toJson(Object o) {
        try {
            return json.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
