package com.connectsphere.post.controller;

import com.connectsphere.post.dto.ApiMessageResponse;
import com.connectsphere.post.dto.ChangeVisibilityRequest;
import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.PostCountResponse;
import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping({"/api/v1/posts", "/posts"})
@Tag(name = "Post Service")
public class PostResource {

    private final PostService postService;

    public PostResource(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "Create a post", description = "Creates a new post with content, optional media URLs, type, and visibility.")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (request.authorId() == null || !request.authorId().trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a post by id", description = "Returns one post if it exists and has not been soft-deleted.")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable String postId,
            @RequestHeader(value = "X-User-Id", required = false) String viewerId,
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(postService.getPostById(postId, viewerId, viewerRole));
    }

    @GetMapping("/user/{authorId}")
    @Operation(summary = "Get posts by author", description = "Returns an author's timeline ordered from newest to oldest.")
    public ResponseEntity<List<PostResponse>> getPostsByUser(
            @PathVariable String authorId,
            @RequestHeader(value = "X-User-Id", required = false) String viewerId,
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(postService.getPostsByUser(authorId, viewerId, viewerRole));
    }

    @GetMapping("/feed")
    @Operation(summary = "Get a feed", description = "Builds a feed from the list of followed user ids supplied as query parameters.")
    public ResponseEntity<List<PostResponse>> getFeed(
            @RequestParam(required = false) List<String> userIds,
            @RequestHeader(value = "X-User-Id", required = false) String viewerId,
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(postService.getFeedForUser(userIds, viewerId, viewerRole));
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts", description = "Finds posts whose content contains the supplied search text.")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @RequestParam("query") String query,
            @RequestHeader(value = "X-User-Id", required = false) String viewerId,
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(postService.searchPosts(query, viewerId, viewerRole));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update a post", description = "Edits a post's content, media URLs, and type.")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String postId,
            @Valid @RequestBody UpdatePostRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, request, actorId, actorRole));
    }

    @PutMapping("/{postId}/visibility")
    @Operation(summary = "Change post visibility", description = "Updates whether the post is public, private, or followers-only.")
    public ResponseEntity<PostResponse> changeVisibility(
            @PathVariable String postId,
            @Valid @RequestBody ChangeVisibilityRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(postService.changeVisibility(postId, request.visibility(), actorId, actorRole));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Soft-delete a post", description = "Marks the post as deleted so it stops appearing in normal reads and feeds.")
    public ResponseEntity<ApiMessageResponse> deletePost(
            @PathVariable String postId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        postService.deletePost(postId, actorId, actorRole);
        return ResponseEntity.ok(new ApiMessageResponse("Post soft-deleted successfully."));
    }

    @PostMapping("/{postId}/likes/increment")
    @Operation(summary = "Increment likes", description = "Adds one to the likes counter for the selected post.")
    public ResponseEntity<PostResponse> incrementLikes(@PathVariable String postId) {
        return ResponseEntity.ok(postService.incrementLikes(postId));
    }

    @PostMapping("/{postId}/likes/decrement")
    @Operation(summary = "Decrement likes", description = "Subtracts one from the likes counter when the counter is above zero.")
    public ResponseEntity<PostResponse> decrementLikes(@PathVariable String postId) {
        return ResponseEntity.ok(postService.decrementLikes(postId));
    }

    @PostMapping("/{postId}/comments/increment")
    @Operation(summary = "Increment comments", description = "Adds one to the comments counter for the selected post.")
    public ResponseEntity<PostResponse> incrementComments(@PathVariable String postId) {
        return ResponseEntity.ok(postService.incrementComments(postId));
    }

    @GetMapping("/count/{authorId}")
    @Operation(summary = "Count an author's posts", description = "Returns how many non-deleted posts belong to the supplied author.")
    public ResponseEntity<PostCountResponse> getPostCount(
            @PathVariable String authorId,
            @RequestHeader(value = "X-User-Id", required = false) String viewerId,
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(new PostCountResponse(authorId, postService.getPostCount(authorId, viewerId, viewerRole)));
    }
}
