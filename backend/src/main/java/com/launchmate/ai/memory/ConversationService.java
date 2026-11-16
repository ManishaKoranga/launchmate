package com.launchmate.ai.memory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persistence for conversations. Ownership checks are part of every lookup. */
@Service
public class ConversationService {

    private final ConversationRepository conversations;
    private final ConversationMessageRepository messages;
    private final Clock clock;

    public ConversationService(ConversationRepository conversations, ConversationMessageRepository messages, Clock clock) {
        this.conversations = conversations;
        this.messages = messages;
        this.clock = clock;
    }

    @Transactional
    public Conversation create(UUID userId, UUID businessId, ConversationKind kind) {
        return conversations.save(new Conversation(userId, businessId, kind));
    }

    @Transactional
    public ConversationMessage append(Conversation conversation, MessageRole role, String content, Map<String, Object> metadata) {
        conversation.touch();
        Instant now = clock.instant();
        return messages.save(new ConversationMessage(conversation.getId(), role, content, metadata, now));
    }

    @Transactional(readOnly = true)
    public List<ConversationMessage> messages(UUID conversationId) {
        return messages.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional(readOnly = true)
    public long messageCount(UUID conversationId) {
        return messages.countByConversationId(conversationId);
    }
}
