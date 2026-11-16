package com.launchmate.onboarding.service;

import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.catalogue.domain.ProductInput;

import java.util.List;

/** Port: understands the owner's free-text (English/Hindi/Hinglish) description of their business. */
public interface BusinessProfileExtractor {

    Extraction extract(BusinessProfile currentDraft, List<ProductInput> currentOfferings,
                       List<OnboardingTurn> history, String newMessage);

    /**
     * @param reply the assistant's next message, in the owner's language style
     */
    record Extraction(BusinessProfile profile, List<ProductInput> offerings, String reply) {
    }
}
