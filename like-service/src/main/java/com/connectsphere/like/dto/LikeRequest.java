package com.connectsphere.like.dto;

import com.connectsphere.like.entity.ReactionType;
import com.connectsphere.like.entity.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LikeRequest(
        @NotBlank String userId,
        @NotBlank String targetId,
        @NotNull TargetType targetType,
        @NotNull ReactionType reactionType
) {
}
