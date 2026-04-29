package com.connectsphere.post.dto;

import com.connectsphere.post.entity.PostType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdatePostRequest(
        @Size(max = 5000) String content,
        List<@Size(max = 1000) String> mediaUrls,
        @NotNull PostType postType
) {
}
