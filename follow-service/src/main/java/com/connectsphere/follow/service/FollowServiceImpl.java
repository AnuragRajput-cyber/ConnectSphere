package com.connectsphere.follow.service;

import com.connectsphere.follow.dto.FollowRequest;
import com.connectsphere.follow.dto.FollowRelationshipResponse;
import com.connectsphere.follow.dto.FollowResponse;
import com.connectsphere.follow.entity.Follow;
import com.connectsphere.follow.entity.FollowStatus;
import com.connectsphere.follow.exception.BadRequestException;
import com.connectsphere.follow.messaging.NotificationEventPublisher;
import com.connectsphere.follow.messaging.SocialNotificationEvent;
import com.connectsphere.follow.exception.NotFoundException;
import com.connectsphere.follow.repository.FollowRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    public FollowServiceImpl(
            FollowRepository followRepository,
            NotificationEventPublisher notificationEventPublisher
    ) {
        this.followRepository = followRepository;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Override
    public FollowResponse follow(FollowRequest request, String actorId, String actorRole) {
        ensureActorMatches(request.followerId(), actorId, actorRole);
        if (request.followerId().trim().equals(request.followeeId().trim())) {
            throw new BadRequestException("Users cannot follow themselves.");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(request.followerId().trim(), request.followeeId().trim())) {
            throw new BadRequestException("Follow relationship already exists.");
        }
        Follow follow = new Follow();
        follow.setFollowerId(request.followerId().trim());
        follow.setFolloweeId(request.followeeId().trim());
        follow.setStatus(request.status());
        Follow savedFollow = followRepository.save(follow);
        createFollowNotification(savedFollow, savedFollow.getStatus() == FollowStatus.PENDING);
        return FollowResponse.from(savedFollow);
    }

    @Override
    public void unfollow(String followerId, String followeeId, String actorId, String actorRole) {
        ensureActorMatches(followerId, actorId, actorRole);
        if (!followRepository.existsByFollowerIdAndFolloweeId(followerId.trim(), followeeId.trim())) {
            throw new BadRequestException("Follow relationship does not exist.");
        }
        followRepository.deleteByFollowerIdAndFolloweeId(followerId.trim(), followeeId.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(String followerId, String followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeIdAndStatus(
                followerId.trim(),
                followeeId.trim(),
                FollowStatus.ACTIVE
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FollowRelationshipResponse getRelationship(String followerId, String followeeId) {
        return followRepository.findByFollowerIdAndFolloweeId(followerId.trim(), followeeId.trim())
                .map(follow -> new FollowRelationshipResponse(
                        true,
                        follow.getStatus() == FollowStatus.ACTIVE,
                        follow.getStatus() == FollowStatus.PENDING,
                        follow.getFollowId(),
                        follow.getStatus().name()
                ))
                .orElseGet(() -> new FollowRelationshipResponse(false, false, false, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowers(String followeeId) {
        return followRepository.findByFolloweeIdAndStatus(followeeId.trim(), FollowStatus.ACTIVE).stream()
                .map(FollowResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowing(String followerId) {
        return followRepository.findByFollowerIdAndStatus(followerId.trim(), FollowStatus.ACTIVE).stream()
                .map(FollowResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getPendingRequests(String followeeId) {
        return followRepository.findByFolloweeIdAndStatus(followeeId.trim(), FollowStatus.PENDING).stream()
                .map(FollowResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getOutgoingPendingRequests(String followerId) {
        return followRepository.findByFollowerIdAndStatus(followerId.trim(), FollowStatus.PENDING).stream()
                .map(FollowResponse::from)
                .toList();
    }

    @Override
    public FollowResponse acceptRequest(String followId, String actorId, String actorRole) {
        Follow follow = followRepository.findById(followId.trim())
                .orElseThrow(() -> new NotFoundException("Follow request not found."));
        ensureActorMatches(follow.getFolloweeId(), actorId, actorRole);
        if (follow.getStatus() != FollowStatus.PENDING) {
            throw new BadRequestException("Only pending follow requests can be accepted.");
        }
        follow.setStatus(FollowStatus.ACTIVE);
        Follow saved = followRepository.save(follow);
        createAcceptedNotification(saved);
        return FollowResponse.from(saved);
    }

    @Override
    public void rejectRequest(String followId, String actorId, String actorRole) {
        Follow follow = followRepository.findById(followId.trim())
                .orElseThrow(() -> new NotFoundException("Follow request not found."));
        ensureActorMatches(follow.getFolloweeId(), actorId, actorRole);
        if (follow.getStatus() != FollowStatus.PENDING) {
            throw new BadRequestException("Only pending follow requests can be rejected.");
        }
        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowerCount(String followeeId) {
        return followRepository.countByFolloweeIdAndStatus(followeeId.trim(), FollowStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowingCount(String followerId) {
        return followRepository.countByFollowerIdAndStatus(followerId.trim(), FollowStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMutualFollows(String userId) {
        return followRepository.findMutualFollows(userId.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSuggestedUsers(String userId) {
        Set<String> following = followRepository.findByFollowerId(userId.trim()).stream()
                .filter(follow -> follow.getStatus() == FollowStatus.ACTIVE)
                .map(Follow::getFolloweeId)
                .collect(java.util.stream.Collectors.toSet());

        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        for (String followeeId : following) {
            followRepository.findByFollowerId(followeeId).stream()
                    .filter(follow -> follow.getStatus() == FollowStatus.ACTIVE)
                    .map(Follow::getFolloweeId)
                    .filter(candidate -> !candidate.equals(userId.trim()))
                    .filter(candidate -> !following.contains(candidate))
                    .forEach(suggestions::add);
        }
        return suggestions.stream().limit(10).toList();
    }

    private void createFollowNotification(Follow follow, boolean request) {
        try {
            notificationEventPublisher.publish(new SocialNotificationEvent(
                    follow.getFolloweeId(),
                    follow.getFollowerId(),
                    request ? "FOLLOW_REQUEST" : "FOLLOW",
                    request ? "requested to follow you" : "started following you",
                    follow.getFollowerId(),
                    "USER",
                    null
            ));
        } catch (RuntimeException ignored) {
            // The follow relationship should still be stored even if notification delivery is temporarily unavailable.
        }
    }

    private void createAcceptedNotification(Follow follow) {
        try {
            notificationEventPublisher.publish(new SocialNotificationEvent(
                    follow.getFollowerId(),
                    follow.getFolloweeId(),
                    "FOLLOW_ACCEPTED",
                    "accepted your follow request",
                    follow.getFolloweeId(),
                    "USER",
                    null
            ));
        } catch (RuntimeException ignored) {
            // Acceptance should not fail just because notification delivery is unavailable.
        }
    }

    private void ensureActorMatches(String expectedUserId, String actorId, String actorRole) {
        if (isAdmin(actorRole)) {
            return;
        }
        if (actorId == null || actorId.isBlank()) {
            throw new NotFoundException("Follow relationship not found.");
        }
        if (expectedUserId == null || !expectedUserId.trim().equalsIgnoreCase(actorId.trim())) {
            throw new NotFoundException("Follow relationship not found.");
        }
    }

    private boolean isAdmin(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN");
    }
}
