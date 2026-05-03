package com.connectsphere.post.dto;

import com.connectsphere.post.entity.Post;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record PostResponse(
        String postId,
        String authorId,
        String content,
        List<String> mediaUrls,
        String postType,
        String visibility,
        long likesCount,
        long commentsCount,
        long sharesCount,
        Instant createdAt,
        Instant updatedAt,
        boolean deleted
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getPostId(),
                post.getAuthorId(),
                post.getContent(),
                new ArrayList<>(post.getMediaUrls()),
                post.getPostType().name(),
                post.getVisibility().name(),
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getSharesCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.isDeleted()
        );
    }
}
