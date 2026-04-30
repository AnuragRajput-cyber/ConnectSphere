package com.connectsphere.chat.dto;

import com.connectsphere.chat.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageResponse(
        String messageId,
        String conversationId,
        String senderId,
        String recipientId,
        String content,
        Instant sentAt,
        boolean read
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getMessageId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getContent(),
                message.getSentAt(),
                message.isRead()
        );
    }
}
