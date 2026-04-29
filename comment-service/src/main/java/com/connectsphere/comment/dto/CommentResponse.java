package com.connectsphere.comment.dto;

import com.connectsphere.comment.entity.Comment;
import java.time.Instant;

public record CommentResponse(
        String commentId,
        String postId,
        String authorId,
        String parentCommentId,
        String content,
        int likesCount,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getLikesCount(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
