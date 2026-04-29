package com.connectsphere.like.messaging;

public record SocialNotificationEvent(
        String recipientId,
        String actorId,
        String type,
        String message,
        String targetId,
        String targetType,
        String deepLinkUrl
) {
}
