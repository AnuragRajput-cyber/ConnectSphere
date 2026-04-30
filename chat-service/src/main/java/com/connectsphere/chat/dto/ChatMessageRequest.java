package com.connectsphere.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        String conversationId,
        @NotBlank String senderId,
        @NotBlank String recipientId,
        @NotBlank String content
) {
}
