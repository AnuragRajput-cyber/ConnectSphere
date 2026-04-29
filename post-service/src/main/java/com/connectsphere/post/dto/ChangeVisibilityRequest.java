package com.connectsphere.post.dto;

import com.connectsphere.post.entity.PostVisibility;
import jakarta.validation.constraints.NotNull;

public record ChangeVisibilityRequest(@NotNull PostVisibility visibility) {
}
