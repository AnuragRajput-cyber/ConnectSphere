package com.connectsphere.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ConversationRequest(@NotBlank String participantOneId, @NotBlank String participantTwoId) {
}
