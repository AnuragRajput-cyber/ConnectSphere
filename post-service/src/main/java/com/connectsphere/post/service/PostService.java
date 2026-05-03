package com.connectsphere.post.service;

import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.entity.PostVisibility;
import java.util.List;

public interface PostService {

    PostResponse createPost(CreatePostRequest request);

    default PostResponse getPostById(String postId) {
        return getPostById(postId, null, null);
    }

    PostResponse getPostById(String postId, String viewerId, String viewerRole);

    default List<PostResponse> getPostsByUser(String authorId) {
        return getPostsByUser(authorId, null, null);
    }

    List<PostResponse> getPostsByUser(String authorId, String viewerId, String viewerRole);

    default List<PostResponse> getFeedForUser(List<String> userIds) {
        return getFeedForUser(userIds, null, null);
    }

    List<PostResponse> getFeedForUser(List<String> userIds, String viewerId, String viewerRole);

    default PostResponse updatePost(String postId, UpdatePostRequest request) {
        return updatePost(postId, request, null, null);
    }

    PostResponse updatePost(String postId, UpdatePostRequest request, String actorId, String actorRole);

    default void deletePost(String postId) {
        deletePost(postId, null, null);
    }

    void deletePost(String postId, String actorId, String actorRole);

    default List<PostResponse> searchPosts(String query) {
        return searchPosts(query, null, null);
    }

    List<PostResponse> searchPosts(String query, String viewerId, String viewerRole);

    PostResponse incrementLikes(String postId);

    PostResponse decrementLikes(String postId);

    PostResponse incrementComments(String postId);

    default PostResponse changeVisibility(String postId, PostVisibility visibility) {
        return changeVisibility(postId, visibility, null, null);
    }

    PostResponse changeVisibility(String postId, PostVisibility visibility, String actorId, String actorRole);

    default long getPostCount(String authorId) {
        return getPostCount(authorId, null, null);
    }

    long getPostCount(String authorId, String viewerId, String viewerRole);
}
