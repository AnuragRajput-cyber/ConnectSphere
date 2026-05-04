package com.connectsphere.like.service;

import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.entity.Like;
import com.connectsphere.like.entity.ReactionType;
import com.connectsphere.like.entity.TargetType;
import com.connectsphere.like.exception.BadRequestException;
import com.connectsphere.like.messaging.NotificationEventPublisher;
import com.connectsphere.like.messaging.SocialNotificationEvent;
import com.connectsphere.like.repository.LikeRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class LikeServiceImpl implements LikeService {

    private static final String AUTHOR_ID_FIELD = "authorId";

    private final LikeRepository likeRepository;
    private final RestClient restClient;
    private final String postServiceBaseUrl;
    private final String commentServiceBaseUrl;
    private final String mediaServiceBaseUrl;
    private final NotificationEventPublisher notificationEventPublisher;

    public LikeServiceImpl(
            LikeRepository likeRepository,
            @Value("${app.services.post-base-url:http://localhost:8082}") String postServiceBaseUrl,
            @Value("${app.services.comment-base-url:http://localhost:8083}") String commentServiceBaseUrl,
            @Value("${app.services.media-base-url:http://localhost:8087}") String mediaServiceBaseUrl,
            NotificationEventPublisher notificationEventPublisher
    ) {
        this.likeRepository = likeRepository;
        this.restClient = RestClient.builder().build();
        this.postServiceBaseUrl = postServiceBaseUrl;
        this.commentServiceBaseUrl = commentServiceBaseUrl;
        this.mediaServiceBaseUrl = mediaServiceBaseUrl;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Override
    public LikeResponse likeTarget(LikeRequest request) {
        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(
                request.userId().trim(), request.targetId().trim(), request.targetType())) {
            throw new BadRequestException("The user has already reacted to this target.");
        }

        Like like = new Like();
        like.setUserId(request.userId().trim());
        like.setTargetId(request.targetId().trim());
        like.setTargetType(request.targetType());
        like.setReactionType(request.reactionType());
        Like saved = likeRepository.save(like);

        // Keep aggregate counters and notifications consistent even if a different client calls like-service directly.
        trySyncCounts(saved, true);
        tryCreateNotification(saved, true);

        return LikeResponse.from(saved);
    }

    @Override
    public void unlikeTarget(String userId, String targetId, TargetType targetType) {
        if (!likeRepository.existsByUserIdAndTargetIdAndTargetType(userId.trim(), targetId.trim(), targetType)) {
            throw new BadRequestException("Reaction does not exist.");
        }
        likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId.trim(), targetId.trim(), targetType);
        Like deleted = new Like();
        deleted.setUserId(userId.trim());
        deleted.setTargetId(targetId.trim());
        deleted.setTargetType(targetType);
        deleted.setReactionType(ReactionType.LIKE);
        trySyncCounts(deleted, false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasLiked(String userId, String targetId, TargetType targetType) {
        return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId.trim(), targetId.trim(), targetType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LikeResponse> getLikesByTarget(String targetId, TargetType targetType) {
        return likeRepository.findByTargetIdAndTargetType(targetId.trim(), targetType).stream()
                .map(LikeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LikeResponse> getLikesByUser(String userId) {
        return likeRepository.findByUserId(userId.trim()).stream()
                .map(LikeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getLikeCount(String targetId) {
        return likeRepository.countByTargetId(targetId.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public long getLikeCountByType(String targetId, TargetType targetType) {
        return likeRepository.countByTargetIdAndTargetType(targetId.trim(), targetType);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getReactionSummary(String targetId, TargetType targetType) {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Object[] row : likeRepository.getReactionSummary(targetId.trim(), targetType)) {
            summary.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return summary;
    }

    @Override
    public LikeResponse changeReaction(String userId, String targetId, TargetType targetType, ReactionType reactionType) {
        Like like = likeRepository.findByUserIdAndTargetIdAndTargetType(userId.trim(), targetId.trim(), targetType)
                .orElseThrow(() -> new BadRequestException("Reaction does not exist."));
        like.setReactionType(reactionType);
        return LikeResponse.from(likeRepository.save(like));
    }

    private void trySyncCounts(Like like, boolean increment) {
        try {
            switch (like.getTargetType()) {
                case POST -> restClient.post()
                        .uri(postServiceBaseUrl + "/api/v1/posts/{postId}/likes/" + (increment ? "increment" : "decrement"), like.getTargetId())
                        .retrieve()
                        .toBodilessEntity();
                case COMMENT -> restClient.post()
                        .uri(commentServiceBaseUrl + "/api/v1/comments/{commentId}/likes" + (increment ? "" : "/remove"), like.getTargetId())
                        .retrieve()
                        .toBodilessEntity();
                case STORY -> {
                    // Story likes are tracked in like-service; story-service does not maintain a like counter.
                }
            }
        } catch (RuntimeException ignored) {
            // Reaction persistence should not depend on downstream counter updates.
        }
    }

    private void tryCreateNotification(Like like, boolean created) {
        if (!created) {
            return;
        }

        try {
            String recipientId = resolveRecipient(like);
            if (recipientId == null || recipientId.isBlank() || recipientId.equals(like.getUserId())) {
                return;
            }

            String targetType = like.getTargetType().name();
            String message = switch (like.getTargetType()) {
                case POST -> "reacted to your post";
                case COMMENT -> "reacted to your comment";
                case STORY -> "reacted to your story";
            };

            notificationEventPublisher.publish(new SocialNotificationEvent(
                    recipientId,
                    like.getUserId(),
                    "LIKE",
                    message,
                    like.getTargetId(),
                    targetType,
                    null
            ));
        } catch (RuntimeException ignored) {
            // Notifications are best-effort; reactions must still succeed during partial outages.
        }
    }

    private String resolveRecipient(Like like) {
        return switch (like.getTargetType()) {
            case POST -> fetchField(postServiceBaseUrl + "/api/v1/posts/" + like.getTargetId(), AUTHOR_ID_FIELD);
            case COMMENT -> fetchField(commentServiceBaseUrl + "/api/v1/comments/" + like.getTargetId(), AUTHOR_ID_FIELD);
            case STORY -> fetchField(mediaServiceBaseUrl + "/api/v1/stories/" + like.getTargetId(), AUTHOR_ID_FIELD);
        };
    }

    @SuppressWarnings("unchecked")
    private String fetchField(String url, String field) {
        Map<String, Object> body = restClient.get().uri(url).retrieve().body(Map.class);
        if (body == null) {
            return null;
        }
        Object value = body.get(field);
        return value == null ? null : value.toString();
    }
}
