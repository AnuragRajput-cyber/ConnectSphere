package com.connectsphere.search.dto;

import com.connectsphere.search.entity.Hashtag;
import java.time.Instant;

public record HashtagResponse(String hashtagId, String tag, long postCount, Instant lastUsedAt) {
    public static HashtagResponse from(Hashtag hashtag) {
        return new HashtagResponse(hashtag.getHashtagId(), hashtag.getTag(), hashtag.getPostCount(), hashtag.getLastUsedAt());
    }
}
