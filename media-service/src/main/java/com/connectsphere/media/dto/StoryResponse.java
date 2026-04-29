package com.connectsphere.media.dto;

import com.connectsphere.media.entity.Story;
import java.time.Instant;

public record StoryResponse(
        String storyId,
        String authorId,
        String mediaUrl,
        String caption,
        String mediaType,
        long viewsCount,
        Instant expiresAt,
        Instant createdAt,
        boolean active
) {
    public static StoryResponse from(Story story) {
        return new StoryResponse(
                story.getStoryId(),
                story.getAuthorId(),
                story.getMediaUrl(),
                story.getCaption(),
                story.getMediaType().name(),
                story.getViewsCount(),
                story.getExpiresAt(),
                story.getCreatedAt(),
                story.isActive()
        );
    }
}
