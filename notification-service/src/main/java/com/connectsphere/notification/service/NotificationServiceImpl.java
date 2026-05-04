package com.connectsphere.notification.service;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.NotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.entity.Notification;
import com.connectsphere.notification.exception.NotFoundException;
import com.connectsphere.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final String FEED_PATH = "/feed";
    private static final String NOTIFICATIONS_PATH = "/notifications";

    private final NotificationRepository notificationRepository;
    private final CacheManager cacheManager;

    public NotificationServiceImpl(NotificationRepository notificationRepository, CacheManager cacheManager) {
        this.notificationRepository = notificationRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        NotificationResponse response = NotificationResponse.from(notificationRepository.save(toEntity(request)));
        evictRecipientCaches(response.recipientId());
        return response;
    }

    @Override
    public List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request) {
        List<NotificationResponse> responses = request.recipientIds().stream()
                .map(recipientId -> new NotificationRequest(
                        recipientId,
                        request.actorId(),
                        request.type(),
                        request.message(),
                        request.targetId(),
                        request.targetType(),
                        request.deepLinkUrl()
                ))
                .map(this::createNotification)
                .toList();
        request.recipientIds().forEach(this::evictRecipientCaches);
        return responses;
    }

    @Override
    public NotificationResponse markAsRead(String notificationId, String actorId, String actorRole) {
        Notification notification = getNotification(notificationId);
        ensureActorMatches(notification.getRecipientId(), actorId, actorRole);
        notification.setRead(true);
        NotificationResponse response = NotificationResponse.from(notificationRepository.save(notification));
        evictRecipientCaches(notification.getRecipientId());
        return response;
    }

    @Override
    public List<NotificationResponse> markAllRead(String recipientId, String actorId, String actorRole) {
        ensureActorMatches(recipientId, actorId, actorRole);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId.trim());
        notifications.forEach(notification -> notification.setRead(true));
        List<NotificationResponse> responses = notificationRepository.saveAll(notifications).stream().map(NotificationResponse::from).toList();
        evictRecipientCaches(recipientId);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "notificationsByRecipient", key = "#recipientId")
    public List<NotificationResponse> getByRecipient(String recipientId, String actorId, String actorRole) {
        ensureActorMatches(recipientId, actorId, actorRole);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId.trim()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "notificationUnreadCount", key = "#recipientId")
    public long getUnreadCount(String recipientId, String actorId, String actorRole) {
        ensureActorMatches(recipientId, actorId, actorRole);
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId.trim());
    }

    @Override
    public void deleteNotification(String notificationId, String actorId, String actorRole) {
        Notification notification = getNotification(notificationId);
        ensureActorMatches(notification.getRecipientId(), actorId, actorRole);
        notificationRepository.deleteByNotificationId(notificationId.trim());
        evictRecipientCaches(notification.getRecipientId());
    }

    @Override
    public String sendEmailAlert(String recipientId, String subject, String body) {
        // This starter returns a traceable message instead of integrating a live SMTP provider.
        return "Email alert queued for " + recipientId + " with subject '" + subject + "'. Body: " + body;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream().map(NotificationResponse::from).toList();
    }

    private Notification toEntity(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipientId(request.recipientId().trim());
        notification.setActorId(blankToNull(request.actorId()));
        notification.setType(request.type());
        notification.setMessage(request.message().trim());
        notification.setTargetId(blankToNull(request.targetId()));
        notification.setTargetType(blankToNull(request.targetType()));
        notification.setDeepLinkUrl(resolveDeepLinkUrl(request));
        return notification;
    }

    private String resolveDeepLinkUrl(NotificationRequest request) {
        String explicit = blankToNull(request.deepLinkUrl());
        if (explicit != null) {
            return explicit;
        }

        String targetType = blankToNull(request.targetType());
        String targetId = blankToNull(request.targetId());
        if (targetType == null) {
            return NOTIFICATIONS_PATH;
        }

        return switch (targetType.toUpperCase(Locale.ROOT)) {
            case "POST" -> targetId == null ? FEED_PATH : "/post/" + targetId;
            case "USER" -> targetId == null ? FEED_PATH : "/profile/" + targetId;
            case "STORY" -> targetId == null ? FEED_PATH : FEED_PATH + "?story=" + targetId;
            case "SYSTEM" -> NOTIFICATIONS_PATH;
            default -> NOTIFICATIONS_PATH;
        };
    }

    private Notification getNotification(String notificationId) {
        return notificationRepository.findById(notificationId.trim())
                .orElseThrow(() -> new NotFoundException("Notification not found."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void ensureActorMatches(String expectedRecipientId, String actorId, String actorRole) {
        if (isAdmin(actorRole)) {
            return;
        }
        if (actorId == null || actorId.isBlank()) {
            throw new NotFoundException("Notification not found.");
        }
        if (expectedRecipientId == null || !expectedRecipientId.trim().equalsIgnoreCase(actorId.trim())) {
            throw new NotFoundException("Notification not found.");
        }
    }

    private boolean isAdmin(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN");
    }

    private void evictRecipientCaches(String recipientId) {
        if (recipientId == null || recipientId.isBlank()) {
            return;
        }
        for (String cacheName : List.of("notificationsByRecipient", "notificationUnreadCount")) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(recipientId.trim());
            }
        }
    }
}
