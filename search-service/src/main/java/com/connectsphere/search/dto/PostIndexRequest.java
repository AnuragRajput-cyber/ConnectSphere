package com.connectsphere.search.dto;

import jakarta.validation.constraints.NotBlank;

public record PostIndexRequest(@NotBlank String postId, @NotBlank String content) {
}
