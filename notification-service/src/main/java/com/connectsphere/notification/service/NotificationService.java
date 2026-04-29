package com.connectsphere.notification.service;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.NotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request);

    default NotificationResponse markAsRead(String notificationId) {
        return markAsRead(notificationId, null, null);
    }

    NotificationResponse markAsRead(String notificationId, String actorId, String actorRole);

    default List<NotificationResponse> markAllRead(String recipientId) {
        return markAllRead(recipientId, null, null);
    }

    List<NotificationResponse> markAllRead(String recipientId, String actorId, String actorRole);

    default List<NotificationResponse> getByRecipient(String recipientId) {
        return getByRecipient(recipientId, null, null);
    }

    List<NotificationResponse> getByRecipient(String recipientId, String actorId, String actorRole);

    default long getUnreadCount(String recipientId) {
        return getUnreadCount(recipientId, null, null);
    }

    long getUnreadCount(String recipientId, String actorId, String actorRole);

    default void deleteNotification(String notificationId) {
        deleteNotification(notificationId, null, null);
    }

    void deleteNotification(String notificationId, String actorId, String actorRole);

    String sendEmailAlert(String recipientId, String subject, String body);

    List<NotificationResponse> getAll();
}
