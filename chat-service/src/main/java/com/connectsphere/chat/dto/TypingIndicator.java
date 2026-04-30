package com.connectsphere.chat.dto;

public record TypingIndicator(String conversationId, String senderId, String recipientId, boolean typing) {
}
