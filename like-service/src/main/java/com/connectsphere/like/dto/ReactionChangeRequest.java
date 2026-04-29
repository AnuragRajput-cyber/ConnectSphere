package com.connectsphere.like.dto;

import com.connectsphere.like.entity.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactionChangeRequest(@NotNull ReactionType reactionType) {
}
