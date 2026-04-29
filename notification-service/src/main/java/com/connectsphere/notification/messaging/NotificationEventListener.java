package com.connectsphere.notification.messaging;

import com.connectsphere.notification.dto.NotificationRequest;
import com.connectsphere.notification.entity.NotificationType;
import com.connectsphere.notification.service.NotificationService;
import java.util.Locale;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.events.notification-queue}")
    public void onNotificationEvent(SocialNotificationEvent event) {
        if (event == null || event.recipientId() == null || event.recipientId().isBlank() || event.message() == null || event.message().isBlank()) {
            return;
        }
        notificationService.createNotification(new NotificationRequest(
                event.recipientId(),
                event.actorId(),
                NotificationType.valueOf(event.type().trim().toUpperCase(Locale.ROOT)),
                event.message(),
                event.targetId(),
                event.targetType(),
                event.deepLinkUrl()
        ));
    }
}
