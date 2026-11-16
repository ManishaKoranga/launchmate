package com.launchmate.onboarding.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.ai.memory.Conversation;
import com.launchmate.ai.memory.ConversationKind;
import com.launchmate.ai.memory.ConversationMessage;
import com.launchmate.ai.memory.ConversationRepository;
import com.launchmate.ai.memory.ConversationService;
import com.launchmate.ai.memory.MessageRole;
import com.launchmate.ai.provider.LlmClient;
import com.launchmate.business.domain.Business;
import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.business.domain.Plan;
import com.launchmate.business.domain.PlanLimits;
import com.launchmate.business.service.BusinessService;
import com.launchmate.catalogue.service.ProductService;
import com.launchmate.onboarding.domain.OnboardingState;
import com.launchmate.onboarding.domain.RequiredFields;
import com.launchmate.onboarding.dto.CompleteOnboardingRequest;
import com.launchmate.onboarding.dto.OnboardingResponse;
import com.launchmate.shared.exception.BusinessRuleException;
import com.launchmate.shared.exception.ConflictException;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.shared.ratelimit.AiUsageGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Conversational onboarding: the owner describes the business, the AI extracts a profile and asks for
 * what is missing, the owner reviews and approves. The slow AI call runs outside any DB transaction.
 */
@Service
public class OnboardingService {

    static final int MAX_MESSAGES = 40;
    private static final int HISTORY_TURNS = 20;

    private final ConversationRepository conversations;
    private final ConversationService conversationService;
    private final BusinessProfileExtractor extractor;
    private final BusinessService businesses;
    private final ProductService products;
    private final AiUsageGuard aiUsage;
    private final LlmClient llm;
    private final ObjectMapper json;
    private final TransactionTemplate tx;

    public OnboardingService(ConversationRepository conversations, ConversationService conversationService,
                             BusinessProfileExtractor extractor, BusinessService businesses, ProductService products,
                             AiUsageGuard aiUsage, LlmClient llm, ObjectMapper json, TransactionTemplate tx) {
        this.conversations = conversations;
        this.conversationService = conversationService;
        this.extractor = extractor;
        this.businesses = businesses;
        this.products = products;
        this.aiUsage = aiUsage;
        this.llm = llm;
        this.json = json;
        this.tx = tx;
    }

    @Transactional
    public OnboardingResponse start(UUID userId) {
        Conversation c = conversationService.create(userId, null, ConversationKind.ONBOARDING);
        c.replaceState(toMap(OnboardingState.empty()));
        return toResponse(c);
    }

    /** The user's most recent unfinished onboarding, so a refresh doesn't lose their progress. */
    @Transactional(readOnly = true)
    public Optional<OnboardingResponse> latestOpen(UUID userId) {
        return conversations.findFirstByUserIdAndKindAndBusinessIdIsNullOrderByUpdatedAtDesc(userId, ConversationKind.ONBOARDING)
                .filter(c -> !state(c).isCompleted())
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OnboardingResponse get(UUID userId, UUID conversationId) {
        return toResponse(load(userId, conversationId));
    }

    public OnboardingResponse sendMessage(UUID userId, UUID conversationId, String text) {
        Conversation conversation = tx.execute(s -> load(userId, conversationId));
        OnboardingState current = state(conversation);
        if (current.isCompleted()) {
            throw new ConflictException("This onboarding is already complete");
        }
        if (conversationService.messageCount(conversationId) >= MAX_MESSAGES) {
            throw new BusinessRuleException("This conversation is long enough — please review your details and continue.");
        }
        aiUsage.acquire(userId, PlanLimits.of(Plan.FREE).aiRequestsPerHour());

        List<OnboardingTurn> history = conversationService.messages(conversationId).stream()
                .filter(m -> m.getRole() != MessageRole.TOOL)
                .map(m -> new OnboardingTurn(m.getRole() == MessageRole.USER, m.getContent()))
                .toList();
        history = history.subList(Math.max(0, history.size() - HISTORY_TURNS), history.size());

        BusinessProfileExtractor.Extraction extraction =
                extractor.extract(current.profile(), current.offerings(), history, text);

        BusinessProfile merged = merge(current.profile(), extraction.profile());
        List<String> missing = RequiredFields.missing(merged);
        OnboardingState next = new OnboardingState(merged, extraction.offerings(), missing,
                missing.isEmpty(), null);

        return tx.execute(s -> {
            Conversation c = load(userId, conversationId);
            conversationService.append(c, MessageRole.USER, text, null);
            conversationService.append(c, MessageRole.ASSISTANT, extraction.reply(), null);
            c.replaceState(toMap(next));
            return toResponse(c);
        });
    }

    /** Creates the business from the owner-approved profile and offerings. */
    @Transactional
    public Business complete(UUID userId, UUID conversationId, CompleteOnboardingRequest request) {
        Conversation c = load(userId, conversationId);
        OnboardingState current = state(c);
        if (current.isCompleted()) {
            throw new ConflictException("This onboarding is already complete");
        }
        Business business = businesses.create(userId, request.profile());
        businesses.confirmProfile(business.getId(), userId);
        if (!request.offerings().isEmpty()) {
            products.createAll(business.getId(), userId, request.offerings());
        }
        c.linkBusiness(business.getId());
        c.replaceState(toMap(new OnboardingState(request.profile(), request.offerings(), List.of(), true,
                business.getId())));
        return business;
    }

    private Conversation load(UUID userId, UUID conversationId) {
        return conversations.findByIdAndUserIdAndKind(conversationId, userId, ConversationKind.ONBOARDING)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding"));
    }

    /** Model output never erases something the owner already told us. */
    static BusinessProfile merge(BusinessProfile prev, BusinessProfile next) {
        if (prev == null) {
            return next;
        }
        if (next == null) {
            return prev;
        }
        return new BusinessProfile(
                pick(next.name(), prev.name()),
                next.category() != null ? next.category() : prev.category(),
                pick(next.tagline(), prev.tagline()),
                pick(next.description(), prev.description()),
                pick(next.city(), prev.city()),
                pick(next.area(), prev.area()),
                pick(next.address(), prev.address()),
                pick(next.phone(), prev.phone()),
                pick(next.whatsapp(), prev.whatsapp()),
                pick(next.email(), prev.email()),
                pick(next.targetCustomers(), prev.targetCustomers()),
                pick(next.languages(), prev.languages()),
                next.openingHours() != null ? next.openingHours() : prev.openingHours());
    }

    private static String pick(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }

    private OnboardingState state(Conversation c) {
        Map<String, Object> raw = c.getState();
        return raw.isEmpty() ? OnboardingState.empty() : json.convertValue(raw, OnboardingState.class);
    }

    private Map<String, Object> toMap(OnboardingState s) {
        return json.convertValue(s, new TypeReference<>() {
        });
    }

    private OnboardingResponse toResponse(Conversation c) {
        OnboardingState s = state(c);
        List<OnboardingResponse.Message> messages = conversationService.messages(c.getId()).stream()
                .filter(m -> m.getRole() != MessageRole.TOOL)
                .map(OnboardingService::toMessage)
                .toList();
        return new OnboardingResponse(c.getId(), s.profile(), s.offerings(), s.missingFields(), s.readyForReview(),
                s.businessId(), messages, llm.isConfigured());
    }

    private static OnboardingResponse.Message toMessage(ConversationMessage m) {
        return new OnboardingResponse.Message(m.getRole().name().toLowerCase(), m.getContent(), m.getCreatedAt());
    }
}
