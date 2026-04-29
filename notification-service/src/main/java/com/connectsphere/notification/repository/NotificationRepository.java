package com.connectsphere.notification.repository;

import com.connectsphere.notification.entity.Notification;
import com.connectsphere.notification.entity.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String recipientId);

    long countByRecipientIdAndReadFalse(String recipientId);

    List<Notification> findByType(NotificationType type);

    Optional<Notification> findByActorIdAndTargetId(String actorId, String targetId);

    void deleteByNotificationId(String notificationId);
}
