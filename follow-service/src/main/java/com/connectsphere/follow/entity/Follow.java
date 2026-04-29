package com.connectsphere.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_follow_pair", columnNames = {"follower_id", "followee_id"})
})
public class Follow {

    @Id
    @Column(name = "follow_id", nullable = false, updatable = false, length = 36)
    private String followId;

    @Column(name = "follower_id", nullable = false, length = 36)
    private String followerId;

    @Column(name = "followee_id", nullable = false, length = 36)
    private String followeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FollowStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (followId == null) {
            followId = UUID.randomUUID().toString();
        }
        createdAt = Instant.now();
    }

    public String getFollowId() {
        return followId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(String followeeId) {
        this.followeeId = followeeId;
    }

    public FollowStatus getStatus() {
        return status;
    }

    public void setStatus(FollowStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
