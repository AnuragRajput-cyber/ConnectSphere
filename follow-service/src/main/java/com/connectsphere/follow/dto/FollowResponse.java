package com.connectsphere.follow.dto;

import com.connectsphere.follow.entity.Follow;
import java.time.Instant;

public record FollowResponse(
        String followId,
        String followerId,
        String followeeId,
        String status,
        Instant createdAt
) {
    public static FollowResponse from(Follow follow) {
        return new FollowResponse(
                follow.getFollowId(),
                follow.getFollowerId(),
                follow.getFolloweeId(),
                follow.getStatus().name(),
                follow.getCreatedAt()
        );
    }
}
