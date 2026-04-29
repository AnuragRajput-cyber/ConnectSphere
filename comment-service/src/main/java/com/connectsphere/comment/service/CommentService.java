package com.connectsphere.comment.service;

import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;
import java.util.List;

public interface CommentService {

    CommentResponse addComment(CreateCommentRequest request);

    List<CommentResponse> getCommentsByPost(String postId);

    CommentResponse getCommentById(String commentId);

    List<CommentResponse> getReplies(String commentId);

    default CommentResponse updateComment(String commentId, UpdateCommentRequest request) {
        return updateComment(commentId, request, null, null);
    }

    CommentResponse updateComment(String commentId, UpdateCommentRequest request, String actorId, String actorRole);

    default void deleteComment(String commentId) {
        deleteComment(commentId, null, null);
    }

    void deleteComment(String commentId, String actorId, String actorRole);

    List<CommentResponse> getCommentsByUser(String authorId);

    CommentResponse likeComment(String commentId);

    CommentResponse unlikeComment(String commentId);

    long getCommentCount(String postId);
}
