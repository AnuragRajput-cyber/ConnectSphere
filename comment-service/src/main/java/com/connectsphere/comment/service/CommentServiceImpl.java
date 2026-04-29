package com.connectsphere.comment.service;

import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;
import com.connectsphere.comment.entity.Comment;
import com.connectsphere.comment.exception.BadRequestException;
import com.connectsphere.comment.messaging.NotificationEventPublisher;
import com.connectsphere.comment.messaging.SocialNotificationEvent;
import com.connectsphere.comment.exception.NotFoundException;
import com.connectsphere.comment.repository.CommentRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final RestClient restClient;
    private final String postServiceBaseUrl;
    private final NotificationEventPublisher notificationEventPublisher;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            @Value("${app.services.post-base-url:http://localhost:8082}") String postServiceBaseUrl,
            NotificationEventPublisher notificationEventPublisher
    ) {
        this.commentRepository = commentRepository;
        this.restClient = RestClient.builder().build();
        this.postServiceBaseUrl = postServiceBaseUrl;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Override
    public CommentResponse addComment(CreateCommentRequest request) {
        Comment parent = null;
        if (request.parentCommentId() != null && !request.parentCommentId().isBlank()) {
            parent = getActiveComment(request.parentCommentId());
            if (!parent.getPostId().equals(request.postId().trim())) {
                throw new BadRequestException("Replies must stay within the same post.");
            }
            if (parent.getParentCommentId() != null) {
                throw new BadRequestException("Only two discussion levels are supported.");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(request.postId().trim());
        comment.setAuthorId(request.authorId().trim());
        comment.setParentCommentId(blankToNull(request.parentCommentId()));
        comment.setContent(request.content().trim());
        comment.setLikesCount(0);
        Comment saved = commentRepository.save(comment);

        tryIncrementPostComments(saved.getPostId());
        tryCreateNotifications(saved, parent);

        return CommentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(String postId) {
        return commentRepository.findTopLevelByPostId(postId.trim()).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(String commentId) {
        return CommentResponse.from(getActiveComment(commentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(String commentId) {
        getActiveComment(commentId);
        return commentRepository.findByParentCommentIdAndDeletedFalseOrderByCreatedAtAsc(commentId.trim()).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Override
    public CommentResponse updateComment(String commentId, UpdateCommentRequest request, String actorId, String actorRole) {
        Comment comment = getActiveComment(commentId);
        ensureCanModify(comment, actorId, actorRole);
        comment.setContent(request.content().trim());
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(String commentId, String actorId, String actorRole) {
        Comment comment = getActiveComment(commentId);
        ensureCanModify(comment, actorId, actorRole);
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUser(String authorId) {
        return commentRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(authorId.trim()).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Override
    public CommentResponse likeComment(String commentId) {
        Comment comment = getActiveComment(commentId);
        comment.setLikesCount(comment.getLikesCount() + 1);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    public CommentResponse unlikeComment(String commentId) {
        Comment comment = getActiveComment(commentId);
        comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(String postId) {
        return commentRepository.countByPostIdAndDeletedFalse(postId.trim());
    }

    private Comment getActiveComment(String commentId) {
        return commentRepository.findByCommentIdAndDeletedFalse(commentId.trim())
                .orElseThrow(() -> new NotFoundException("Comment not found."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void tryIncrementPostComments(String postId) {
        try {
            restClient.post()
                    .uri(postServiceBaseUrl + "/api/v1/posts/{postId}/comments/increment", postId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Comment persistence should not depend on post-service counter updates.
        }
    }

    private void tryCreateNotifications(Comment saved, Comment parent) {
        try {
            String postAuthorId = fetchField(postServiceBaseUrl + "/api/v1/posts/" + saved.getPostId(), "authorId");
            if (postAuthorId != null && !postAuthorId.isBlank() && !postAuthorId.equals(saved.getAuthorId())) {
                notificationEventPublisher.publish(new SocialNotificationEvent(
                        postAuthorId,
                        saved.getAuthorId(),
                        parent == null ? "COMMENT" : "REPLY",
                        parent == null ? "commented on your post" : "replied to a post you follow",
                        saved.getPostId(),
                        "POST",
                        null
                ));
            }

            if (parent != null && parent.getAuthorId() != null && !parent.getAuthorId().equals(saved.getAuthorId())) {
                notificationEventPublisher.publish(new SocialNotificationEvent(
                        parent.getAuthorId(),
                        saved.getAuthorId(),
                        "REPLY",
                        "replied to your comment",
                        parent.getCommentId(),
                        "COMMENT",
                        null
                ));
            }
        } catch (RuntimeException ignored) {
            // Notification delivery is best-effort.
        }
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

    private void ensureCanModify(Comment comment, String actorId, String actorRole) {
        if (isAdmin(actorRole)) {
            return;
        }
        if (actorId == null || actorId.isBlank()) {
            throw new NotFoundException("Comment not found.");
        }
        if (!comment.getAuthorId().equalsIgnoreCase(actorId.trim())) {
            throw new NotFoundException("Comment not found.");
        }
    }

    private boolean isAdmin(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN");
    }
}
