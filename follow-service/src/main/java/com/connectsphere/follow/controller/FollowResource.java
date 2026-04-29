package com.connectsphere.follow.controller;

import com.connectsphere.follow.dto.ApiMessageResponse;
import com.connectsphere.follow.dto.FollowRequest;
import com.connectsphere.follow.dto.FollowRelationshipResponse;
import com.connectsphere.follow.dto.FollowResponse;
import com.connectsphere.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/follows", "/follows"})
@Tag(name = "Follow Service", description = "Follower and following graph operations.")
public class FollowResource {

    private final FollowService followService;

    public FollowResource(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping
    @Operation(summary = "Create a follow relationship")
    public ResponseEntity<FollowResponse> follow(
            @Valid @RequestBody FollowRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(followService.follow(request, actorId, actorRole));
    }

    @DeleteMapping
    @Operation(summary = "Remove a follow relationship")
    public ResponseEntity<ApiMessageResponse> unfollow(
            @RequestParam String followerId,
            @RequestParam String followeeId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        followService.unfollow(followerId, followeeId, actorId, actorRole);
        return ResponseEntity.ok(new ApiMessageResponse("Unfollowed successfully."));
    }

    @GetMapping("/is-following")
    @Operation(summary = "Check follow state")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@RequestParam String followerId, @RequestParam String followeeId) {
        return ResponseEntity.ok(Map.of("following", followService.isFollowing(followerId, followeeId)));
    }

    @GetMapping("/relationship")
    @Operation(summary = "Get follow relationship details")
    public ResponseEntity<FollowRelationshipResponse> getRelationship(
            @RequestParam String followerId,
            @RequestParam String followeeId
    ) {
        return ResponseEntity.ok(followService.getRelationship(followerId, followeeId));
    }

    @GetMapping("/followers/{followeeId}")
    @Operation(summary = "Get followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable String followeeId) {
        return ResponseEntity.ok(followService.getFollowers(followeeId));
    }

    @GetMapping("/following/{followerId}")
    @Operation(summary = "Get following")
    public ResponseEntity<List<FollowResponse>> getFollowing(@PathVariable String followerId) {
        return ResponseEntity.ok(followService.getFollowing(followerId));
    }

    @GetMapping("/requests/{followeeId}")
    @Operation(summary = "Get incoming follow requests")
    public ResponseEntity<List<FollowResponse>> getPendingRequests(
            @PathVariable String followeeId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (!isAdmin(actorRole) && !followeeId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(followService.getPendingRequests(followeeId));
    }

    @GetMapping("/requests/sent/{followerId}")
    @Operation(summary = "Get outgoing pending follow requests")
    public ResponseEntity<List<FollowResponse>> getOutgoingPendingRequests(
            @PathVariable String followerId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (!isAdmin(actorRole) && !followerId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(followService.getOutgoingPendingRequests(followerId));
    }

    @PatchMapping("/{followId}/accept")
    @Operation(summary = "Accept a pending follow request")
    public ResponseEntity<FollowResponse> acceptRequest(
            @PathVariable String followId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(followService.acceptRequest(followId, actorId, actorRole));
    }

    @DeleteMapping("/{followId}/reject")
    @Operation(summary = "Reject a pending follow request")
    public ResponseEntity<ApiMessageResponse> rejectRequest(
            @PathVariable String followId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        followService.rejectRequest(followId, actorId, actorRole);
        return ResponseEntity.ok(new ApiMessageResponse("Follow request rejected."));
    }

    private boolean isAdmin(String role) {
        return role != null && role.trim().equalsIgnoreCase("ADMIN") || role != null && role.trim().equalsIgnoreCase("ROLE_ADMIN");
    }

    @GetMapping("/followers/{followeeId}/count")
    public ResponseEntity<Map<String, Long>> getFollowerCount(@PathVariable String followeeId) {
        return ResponseEntity.ok(Map.of("count", followService.getFollowerCount(followeeId)));
    }

    @GetMapping("/following/{followerId}/count")
    public ResponseEntity<Map<String, Long>> getFollowingCount(@PathVariable String followerId) {
        return ResponseEntity.ok(Map.of("count", followService.getFollowingCount(followerId)));
    }

    @GetMapping("/mutual/{userId}")
    @Operation(summary = "Get mutual follows")
    public ResponseEntity<List<String>> getMutualFollows(@PathVariable String userId) {
        return ResponseEntity.ok(followService.getMutualFollows(userId));
    }

    @GetMapping("/suggested/{userId}")
    @Operation(summary = "Get suggested users")
    public ResponseEntity<List<String>> getSuggestedUsers(@PathVariable String userId) {
        return ResponseEntity.ok(followService.getSuggestedUsers(userId));
    }
}
