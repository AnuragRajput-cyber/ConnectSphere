package com.connectsphere.search.controller;

import com.connectsphere.search.dto.ApiMessageResponse;
import com.connectsphere.search.dto.HashtagResponse;
import com.connectsphere.search.dto.PostIndexRequest;
import com.connectsphere.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1", ""})
@Tag(name = "Search Service", description = "Delegated user/post search plus hashtag indexing.")
public class SearchResource {

    private final SearchService searchService;

    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search/index")
    @Operation(summary = "Index a post for hashtags")
    public ResponseEntity<Map<String, List<String>>> indexPost(@Valid @RequestBody PostIndexRequest request) {
        return ResponseEntity.ok(Map.of("hashtags", searchService.indexPost(request.postId(), request.content())));
    }

    @DeleteMapping("/search/index/{postId}")
    @Operation(summary = "Remove a post from hashtag index")
    public ResponseEntity<ApiMessageResponse> removePostIndex(@PathVariable String postId) {
        searchService.removePostIndex(postId);
        return ResponseEntity.ok(new ApiMessageResponse("Post index removed successfully."));
    }

    @GetMapping("/search/posts")
    public ResponseEntity<Object> searchPosts(
            @RequestParam String query,
            @RequestHeader(value = "X-User-Id", required = false) String viewerId,
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(searchService.searchPosts(query, viewerId, viewerRole));
    }

    @GetMapping("/search/users")
    public ResponseEntity<Object> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchUsers(query));
    }

    @GetMapping("/hashtags/post/{postId}")
    public ResponseEntity<List<HashtagResponse>> getHashtagsForPost(@PathVariable String postId) {
        return ResponseEntity.ok(searchService.getHashtagsForPost(postId));
    }

    @GetMapping("/hashtags/trending")
    public ResponseEntity<List<HashtagResponse>> getTrendingHashtags() {
        return ResponseEntity.ok(searchService.getTrendingHashtags());
    }

    @GetMapping("/hashtags/{tag}/posts")
    public ResponseEntity<List<String>> getPostsByHashtag(@PathVariable String tag) {
        return ResponseEntity.ok(searchService.getPostsByHashtag(tag));
    }

    @GetMapping("/hashtags/search")
    public ResponseEntity<List<HashtagResponse>> searchHashtags(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchHashtags(query));
    }

    @GetMapping("/hashtags/{tag}/count")
    public ResponseEntity<Map<String, Long>> getHashtagCount(@PathVariable String tag) {
        return ResponseEntity.ok(Map.of("count", searchService.getHashtagCount(tag)));
    }
}
