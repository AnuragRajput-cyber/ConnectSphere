package com.connectsphere.search.service;

import com.connectsphere.search.dto.HashtagResponse;
import java.util.List;

public interface SearchService {

    List<String> indexPost(String postId, String content);

    void removePostIndex(String postId);

    Object searchPosts(String query, String viewerId, String viewerRole);

    Object searchUsers(String query);

    List<HashtagResponse> getHashtagsForPost(String postId);

    List<HashtagResponse> getTrendingHashtags();

    List<String> getPostsByHashtag(String tag);

    List<HashtagResponse> searchHashtags(String query);

    long getHashtagCount(String tag);
}
