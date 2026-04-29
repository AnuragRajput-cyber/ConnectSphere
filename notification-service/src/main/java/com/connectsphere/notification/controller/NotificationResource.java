package com.connectsphere.notification.controller;

import com.connectsphere.notification.dto.ApiMessageResponse;
import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.NotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/notifications", "/notifications"})
@Tag(name = "Notification Service", description = "In-app and bulk notifications.")
public class NotificationResource {

    private final NotificationService notificationService;

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create a notification")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (actorId != null && !actorId.isBlank() && !isAdmin(actorRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Send bulk notifications")
    public ResponseEntity<List<NotificationResponse>> sendBulk(
            @Valid @RequestBody BulkNotificationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (actorId != null && !actorId.isBlank() && !isAdmin(actorRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.sendBulkNotification(request));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark one notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable String notificationId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, actorId, actorRole));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<List<NotificationResponse>> markAllRead(
            @RequestParam String recipientId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(notificationService.markAllRead(recipientId, actorId, actorRole));
    }

    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get notifications by recipient")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(
            @PathVariable String recipientId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId, actorId, actorRole));
    }

    @GetMapping("/recipient/{recipientId}/unread-count")
    @Operation(summary = "Get unread count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable String recipientId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(recipientId, actorId, actorRole)));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiMessageResponse> deleteNotification(
            @PathVariable String notificationId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        notificationService.deleteNotification(notificationId, actorId, actorRole);
        return ResponseEntity.ok(new ApiMessageResponse("Notification deleted successfully."));
    }

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationResponse>> getAll(
            @RequestHeader(value = "X-User-Id", required = false) String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (actorId != null && !actorId.isBlank() && !isAdmin(actorRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.getAll());
    }

    private boolean isAdmin(String role) {
        return role != null && (role.trim().equalsIgnoreCase("ADMIN") || role.trim().equalsIgnoreCase("ROLE_ADMIN"));
    }
}
