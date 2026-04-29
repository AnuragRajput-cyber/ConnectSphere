package com.connectsphere.follow.dto;

import com.connectsphere.follow.entity.FollowStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FollowRequest(
        @NotBlank String followerId,
        @NotBlank String followeeId,
        @NotNull FollowStatus status
) {
}
