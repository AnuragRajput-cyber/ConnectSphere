package com.connectsphere.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hashtags")
public class Hashtag {

    @Id
    @Column(name = "hashtag_id", nullable = false, updatable = false, length = 36)
    private String hashtagId;

    @Column(nullable = false, unique = true, length = 100)
    private String tag;

    @Column(name = "post_count", nullable = false)
    private long postCount;

    @Column(name = "last_used_at", nullable = false)
    private Instant lastUsedAt;

    @PrePersist
    void onCreate() {
        if (hashtagId == null) {
            hashtagId = UUID.randomUUID().toString();
        }
        if (lastUsedAt == null) {
            lastUsedAt = Instant.now();
        }
    }

    public String getHashtagId() {
        return hashtagId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
