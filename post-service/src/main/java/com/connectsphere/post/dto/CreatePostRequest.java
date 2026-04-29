package com.connectsphere.post.dto;

import com.connectsphere.post.entity.PostType;
import com.connectsphere.post.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePostRequest(
        @NotBlank String authorId,
        @Size(max = 5000) String content,
        List<@Size(max = 1000) String> mediaUrls,
        @NotNull PostType postType,
        @NotNull PostVisibility visibility
) {
}
