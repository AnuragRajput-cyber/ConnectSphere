package com.connectsphere.notification.dto;

import com.connectsphere.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotBlank String recipientId,
        String actorId,
        @NotNull NotificationType type,
        @NotBlank String message,
        String targetId,
        String targetType,
        String deepLinkUrl
) {
}
