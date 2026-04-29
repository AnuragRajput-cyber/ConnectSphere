package com.connectsphere.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank String postId,
        @NotBlank String authorId,
        String parentCommentId,
        @NotBlank @Size(max = 1000) String content
) {
}
