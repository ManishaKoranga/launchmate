package com.launchmate.ai.memory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByIdAndUserIdAndKind(UUID id, UUID userId, ConversationKind kind);

    Optional<Conversation> findByIdAndUserIdAndBusinessIdAndKind(UUID id, UUID userId, UUID businessId, ConversationKind kind);

    Optional<Conversation> findFirstByUserIdAndKindAndBusinessIdIsNullOrderByUpdatedAtDesc(UUID userId, ConversationKind kind);

    Optional<Conversation> findFirstByUserIdAndBusinessIdAndKindOrderByUpdatedAtDesc(UUID userId, UUID businessId, ConversationKind kind);
}
