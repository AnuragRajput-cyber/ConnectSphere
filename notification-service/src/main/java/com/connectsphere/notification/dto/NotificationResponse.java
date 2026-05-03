package com.connectsphere.notification.dto;

import com.connectsphere.notification.entity.Notification;
import java.io.Serializable;
import java.time.Instant;

public record NotificationResponse(
        String notificationId,
        String recipientId,
        String actorId,
        String type,
        String message,
        String targetId,
        String targetType,
        String deepLinkUrl,
        boolean read,
        Instant createdAt
) implements Serializable {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getRecipientId(),
                notification.getActorId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getTargetId(),
                notification.getTargetType(),
                notification.getDeepLinkUrl(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
