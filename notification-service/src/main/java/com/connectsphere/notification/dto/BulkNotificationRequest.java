package com.connectsphere.notification.dto;

import com.connectsphere.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkNotificationRequest(
        @NotEmpty List<String> recipientIds,
        String actorId,
        @NotNull NotificationType type,
        @NotBlank String message,
        String targetId,
        String targetType,
        String deepLinkUrl
) {
}
