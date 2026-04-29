package com.connectsphere.auth.dto;

import java.util.List;

public record AdminPlatformOverviewResponse(
        AdminStatsResponse users,
        List<HashtagSummaryResponse> trendingHashtags
) {
}
