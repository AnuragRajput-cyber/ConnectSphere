package com.connectsphere.like.controller;

import com.connectsphere.like.dto.ApiMessageResponse;
import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.dto.ReactionChangeRequest;
import com.connectsphere.like.entity.TargetType;
import com.connectsphere.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/likes", "/likes"})
@Tag(name = "Like Service", description = "Reactions for posts and comments.")
public class LikeResource {

    private final LikeService likeService;

    public LikeResource(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    @Operation(summary = "Create a reaction")
    public ResponseEntity<LikeResponse> likeTarget(
            @Valid @RequestBody LikeRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (!isAdmin(actorRole) && (request.userId() == null || !request.userId().trim().equalsIgnoreCase(actorId.trim()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(likeService.likeTarget(request));
    }

    @DeleteMapping
    @Operation(summary = "Remove a reaction")
    public ResponseEntity<ApiMessageResponse> unlikeTarget(
            @RequestParam String userId,
            @RequestParam String targetId,
            @RequestParam TargetType targetType,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (!isAdmin(actorRole) && !userId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        likeService.unlikeTarget(userId, targetId, targetType);
        return ResponseEntity.ok(new ApiMessageResponse("Reaction removed successfully."));
    }

    @GetMapping("/has-liked")
    @Operation(summary = "Check if a user reacted")
    public ResponseEntity<Map<String, Boolean>> hasLiked(
            @RequestParam String userId,
            @RequestParam String targetId,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(Map.of("liked", likeService.hasLiked(userId, targetId, targetType)));
    }

    @GetMapping("/target/{targetId}")
    @Operation(summary = "Get reactions by target")
    public ResponseEntity<List<LikeResponse>> getLikesByTarget(
            @PathVariable String targetId,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(likeService.getLikesByTarget(targetId, targetType));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reactions by user")
    public ResponseEntity<List<LikeResponse>> getLikesByUser(@PathVariable String userId) {
        return ResponseEntity.ok(likeService.getLikesByUser(userId));
    }

    @GetMapping("/count/{targetId}")
    @Operation(summary = "Count reactions by target id")
    public ResponseEntity<Map<String, Long>> getLikeCount(@PathVariable String targetId) {
        return ResponseEntity.ok(Map.of("count", likeService.getLikeCount(targetId)));
    }

    @GetMapping("/count/{targetId}/type")
    @Operation(summary = "Count reactions by target and type")
    public ResponseEntity<Map<String, Long>> getLikeCountByType(
            @PathVariable String targetId,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(Map.of("count", likeService.getLikeCountByType(targetId, targetType)));
    }

    @GetMapping("/summary/{targetId}")
    @Operation(summary = "Get reaction summary")
    public ResponseEntity<Map<String, Long>> getReactionSummary(
            @PathVariable String targetId,
            @RequestParam TargetType targetType
    ) {
        return ResponseEntity.ok(likeService.getReactionSummary(targetId, targetType));
    }

    @PutMapping("/{userId}/{targetId}")
    @Operation(summary = "Change reaction type")
    public ResponseEntity<LikeResponse> changeReaction(
            @PathVariable String userId,
            @PathVariable String targetId,
            @RequestParam TargetType targetType,
            @Valid @RequestBody ReactionChangeRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (!isAdmin(actorRole) && !userId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(likeService.changeReaction(userId, targetId, targetType, request.reactionType()));
    }

    private boolean isAdmin(String role) {
        return role != null && role.trim().equalsIgnoreCase("ADMIN") || role != null && role.trim().equalsIgnoreCase("ROLE_ADMIN");
    }
}
