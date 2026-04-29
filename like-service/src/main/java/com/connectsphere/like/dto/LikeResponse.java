package com.connectsphere.like.dto;

import com.connectsphere.like.entity.Like;
import java.time.Instant;

public record LikeResponse(
        String likeId,
        String userId,
        String targetId,
        String targetType,
        String reactionType,
        Instant createdAt
) {
    public static LikeResponse from(Like like) {
        return new LikeResponse(
                like.getLikeId(),
                like.getUserId(),
                like.getTargetId(),
                like.getTargetType().name(),
                like.getReactionType().name(),
                like.getCreatedAt()
        );
    }
}
