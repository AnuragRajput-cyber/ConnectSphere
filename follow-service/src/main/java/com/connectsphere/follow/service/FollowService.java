package com.connectsphere.follow.service;

import com.connectsphere.follow.dto.FollowRequest;
import com.connectsphere.follow.dto.FollowRelationshipResponse;
import com.connectsphere.follow.dto.FollowResponse;
import java.util.List;

public interface FollowService {

    default FollowResponse follow(FollowRequest request) {
        return follow(request, null, null);
    }

    FollowResponse follow(FollowRequest request, String actorId, String actorRole);

    default void unfollow(String followerId, String followeeId) {
        unfollow(followerId, followeeId, null, null);
    }

    void unfollow(String followerId, String followeeId, String actorId, String actorRole);

    boolean isFollowing(String followerId, String followeeId);

    FollowRelationshipResponse getRelationship(String followerId, String followeeId);

    List<FollowResponse> getFollowers(String followeeId);

    List<FollowResponse> getFollowing(String followerId);

    List<FollowResponse> getPendingRequests(String followeeId);

    List<FollowResponse> getOutgoingPendingRequests(String followerId);

    default FollowResponse acceptRequest(String followId) {
        return acceptRequest(followId, null, null);
    }

    FollowResponse acceptRequest(String followId, String actorId, String actorRole);

    default void rejectRequest(String followId) {
        rejectRequest(followId, null, null);
    }

    void rejectRequest(String followId, String actorId, String actorRole);

    long getFollowerCount(String followeeId);

    long getFollowingCount(String followerId);

    List<String> getMutualFollows(String userId);

    List<String> getSuggestedUsers(String userId);
}
