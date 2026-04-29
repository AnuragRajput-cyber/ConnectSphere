package com.connectsphere.follow.dto;

public record FollowRelationshipResponse(
        boolean exists,
        boolean following,
        boolean pending,
        String followId,
        String status
) {
}
