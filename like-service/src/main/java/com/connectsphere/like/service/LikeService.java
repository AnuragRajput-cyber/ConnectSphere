package com.connectsphere.like.service;

import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.entity.ReactionType;
import com.connectsphere.like.entity.TargetType;
import java.util.List;
import java.util.Map;

public interface LikeService {

    LikeResponse likeTarget(LikeRequest request);

    void unlikeTarget(String userId, String targetId, TargetType targetType);

    boolean hasLiked(String userId, String targetId, TargetType targetType);

    List<LikeResponse> getLikesByTarget(String targetId, TargetType targetType);

    List<LikeResponse> getLikesByUser(String userId);

    long getLikeCount(String targetId);

    long getLikeCountByType(String targetId, TargetType targetType);

    Map<String, Long> getReactionSummary(String targetId, TargetType targetType);

    LikeResponse changeReaction(String userId, String targetId, TargetType targetType, ReactionType reactionType);
}
