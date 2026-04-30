package com.connectsphere.chat.dto;

import com.connectsphere.chat.entity.Conversation;
import java.time.Instant;

public record ConversationResponse(String conversationId, String participantOneId, String participantTwoId, Instant createdAt) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getConversationId(),
                conversation.getParticipantOneId(),
                conversation.getParticipantTwoId(),
                conversation.getCreatedAt()
        );
    }
}
