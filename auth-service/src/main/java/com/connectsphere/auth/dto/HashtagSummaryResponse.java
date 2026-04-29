package com.connectsphere.auth.dto;

import java.time.Instant;

public record HashtagSummaryResponse(
        String hashtagId,
        String tag,
        long postCount,
        Instant lastUsedAt
) {
}
