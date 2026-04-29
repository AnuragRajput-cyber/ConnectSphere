package com.connectsphere.comment.controller;

import com.connectsphere.comment.dto.ApiMessageResponse;
import com.connectsphere.comment.dto.CommentCountResponse;
import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;
import com.connectsphere.comment.service.CommentService;
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
@RequestMapping({"/api/v1/comments", "/comments"})
@Tag(name = "Comment Service", description = "Threaded comments and replies for post discussions.")
public class CommentResource {

    private final CommentService commentService;

    public CommentResource(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "Add a comment or reply")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(request));
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get top-level comments by post")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable String postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Get one comment")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Get replies for a comment")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request, actorId, actorRole));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Soft-delete a comment")
    public ResponseEntity<ApiMessageResponse> deleteComment(
            @PathVariable String commentId,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        commentService.deleteComment(commentId, actorId, actorRole);
        return ResponseEntity.ok(new ApiMessageResponse("Comment soft-deleted successfully."));
    }

    @GetMapping("/user/{authorId}")
    @Operation(summary = "Get comments by author")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable String authorId) {
        return ResponseEntity.ok(commentService.getCommentsByUser(authorId));
    }

    @PostMapping("/{commentId}/likes")
    @Operation(summary = "Increment comment likes")
    public ResponseEntity<CommentResponse> likeComment(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.likeComment(commentId));
    }

    @PostMapping("/{commentId}/likes/remove")
    @Operation(summary = "Decrement comment likes")
    public ResponseEntity<CommentResponse> unlikeComment(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.unlikeComment(commentId));
    }

    @GetMapping("/count")
    @Operation(summary = "Count comments by post")
    public ResponseEntity<CommentCountResponse> getCommentCount(@RequestParam String postId) {
        return ResponseEntity.ok(new CommentCountResponse(postId, commentService.getCommentCount(postId)));
    }
}
