package com.connectsphere.post.service;

import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.entity.Post;
import com.connectsphere.post.entity.PostType;
import com.connectsphere.post.entity.PostVisibility;
import com.connectsphere.post.exception.BadRequestException;
import com.connectsphere.post.exception.NotFoundException;
import com.connectsphere.post.messaging.SearchIndexEvent;
import com.connectsphere.post.messaging.SearchIndexEventPublisher;
import com.connectsphere.post.repository.PostRepository;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final RestClient restClient;
    private final String followServiceBaseUrl;
    private final SearchIndexEventPublisher searchIndexEventPublisher;
    private final CacheManager cacheManager;

    public PostServiceImpl(
            PostRepository postRepository,
            @org.springframework.beans.factory.annotation.Value("${app.services.follow-base-url:http://localhost:8085}") String followServiceBaseUrl,
            SearchIndexEventPublisher searchIndexEventPublisher,
            CacheManager cacheManager
    ) {
        this.postRepository = postRepository;
        this.restClient = RestClient.builder().build();
        this.followServiceBaseUrl = followServiceBaseUrl;
        this.searchIndexEventPublisher = searchIndexEventPublisher;
        this.cacheManager = cacheManager;
    }

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        validatePostShape(request.content(), request.mediaUrls(), request.postType());

        Post post = new Post();
        post.setAuthorId(request.authorId().trim());
        post.setContent(normalizeContent(request.content()));
        post.setMediaUrls(request.mediaUrls());
        post.setPostType(request.postType());
        post.setVisibility(request.visibility());

        Post savedPost = postRepository.save(post);
        publishSearchIndex(savedPost, "UPSERT");
        clearPostCaches();
        return PostResponse.from(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postById", key = "#postId + '::' + (#viewerId == null ? 'guest' : #viewerId) + '::' + (#viewerRole == null ? 'guest' : #viewerRole)")
    public PostResponse getPostById(String postId, String viewerId, String viewerRole) {
        Post post = requireActivePost(postId);
        if (!canViewPost(post, viewerId, viewerRole, new HashMap<>())) {
            throw new NotFoundException("Post not found.");
        }
        return PostResponse.from(post);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postsByUser", key = "#authorId + '::' + (#viewerId == null ? 'guest' : #viewerId) + '::' + (#viewerRole == null ? 'guest' : #viewerRole)")
    public List<PostResponse> getPostsByUser(String authorId, String viewerId, String viewerRole) {
        String normalizedAuthor = authorId.trim();
        boolean isSelf = viewerId != null && viewerId.trim().equalsIgnoreCase(normalizedAuthor);
        boolean isAdmin = isAdmin(viewerRole);
        Map<String, Boolean> followCache = new HashMap<>();

        return postRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(normalizedAuthor).stream()
                .filter(post -> {
                    if (isAdmin || isSelf) {
                        return true;
                    }
                    return post.getVisibility() == PostVisibility.PUBLIC;
                })
                .filter(post -> post.getVisibility() != PostVisibility.FOLLOWERS_ONLY || canViewPost(post, viewerId, viewerRole, followCache))
                .map(PostResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postFeeds", key = "(#viewerId == null ? 'guest' : #viewerId) + '::' + (#viewerRole == null ? 'guest' : #viewerRole) + '::' + (#userIds == null ? 'public' : #userIds.toString())")
    public List<PostResponse> getFeedForUser(List<String> userIds, String viewerId, String viewerRole) {
        if (userIds == null || userIds.isEmpty()) {
            // Guest/public feed: return the newest public posts only.
            return postRepository.findByVisibilityAndDeletedFalseOrderByCreatedAtDesc(PostVisibility.PUBLIC).stream()
                    .map(PostResponse::from)
                    .toList();
        }

        List<String> normalizedIds = userIds.stream()
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return postRepository.findByVisibilityAndDeletedFalseOrderByCreatedAtDesc(PostVisibility.PUBLIC).stream()
                    .map(PostResponse::from)
                    .toList();
        }

        String effectiveViewerId = viewerId;
        if ((effectiveViewerId == null || effectiveViewerId.isBlank()) && !normalizedIds.isEmpty()) {
            effectiveViewerId = normalizedIds.get(0);
        }
        final String finalViewerId = effectiveViewerId;

        // Personalized feed also includes broader public discovery so new users are not stuck with an empty timeline.
        List<Post> personalized = postRepository.findFeedByUserIds(normalizedIds);
        List<Post> publicPosts = postRepository.findByVisibilityAndDeletedFalseOrderByCreatedAtDesc(PostVisibility.PUBLIC);
        Map<String, Post> merged = new LinkedHashMap<>();
        for (Post post : personalized) {
            merged.put(post.getPostId(), post);
        }
        for (Post post : publicPosts) {
            merged.putIfAbsent(post.getPostId(), post);
        }

        Map<String, Boolean> followCache = new HashMap<>();
        return merged.values().stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .filter(post -> canViewPost(post, finalViewerId, viewerRole, followCache))
                .map(PostResponse::from)
                .toList();
    }

    @Override
    public PostResponse updatePost(String postId, UpdatePostRequest request, String actorId, String actorRole) {
        validatePostShape(request.content(), request.mediaUrls(), request.postType());

        Post post = requireActivePost(postId);
        ensureCanModify(post, actorId, actorRole);
        post.setContent(normalizeContent(request.content()));
        post.setMediaUrls(request.mediaUrls());
        post.setPostType(request.postType());

        Post savedPost = postRepository.save(post);
        publishSearchIndex(savedPost, "UPSERT");
        clearPostCaches();
        return PostResponse.from(savedPost);
    }

    @Override
    public void deletePost(String postId, String actorId, String actorRole) {
        Post post = requireActivePost(postId);
        ensureCanModify(post, actorId, actorRole);
        post.setDeleted(true);
        postRepository.save(post);
        publishSearchIndex(post, "DELETE");
        clearPostCaches();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postSearch", key = "#query + '::' + (#viewerId == null ? 'guest' : #viewerId) + '::' + (#viewerRole == null ? 'guest' : #viewerRole)")
    public List<PostResponse> searchPosts(String query, String viewerId, String viewerRole) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query must not be blank.");
        }

        Map<String, Boolean> followCache = new HashMap<>();
        return postRepository.searchByContent(query.trim()).stream()
                .filter(post -> canViewPost(post, viewerId, viewerRole, followCache))
                .map(PostResponse::from)
                .toList();
    }

    @Override
    public PostResponse incrementLikes(String postId) {
        Post post = requireActivePost(postId);
        post.setLikesCount(post.getLikesCount() + 1);
        PostResponse response = PostResponse.from(postRepository.save(post));
        clearPostCaches();
        return response;
    }

    @Override
    public PostResponse decrementLikes(String postId) {
        Post post = requireActivePost(postId);
        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        PostResponse response = PostResponse.from(postRepository.save(post));
        clearPostCaches();
        return response;
    }

    @Override
    public PostResponse incrementComments(String postId) {
        Post post = requireActivePost(postId);
        post.setCommentsCount(post.getCommentsCount() + 1);
        PostResponse response = PostResponse.from(postRepository.save(post));
        clearPostCaches();
        return response;
    }

    @Override
    public PostResponse changeVisibility(String postId, PostVisibility visibility, String actorId, String actorRole) {
        Post post = requireActivePost(postId);
        ensureCanModify(post, actorId, actorRole);
        post.setVisibility(visibility);
        PostResponse response = PostResponse.from(postRepository.save(post));
        clearPostCaches();
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postCounts", key = "#authorId + '::' + (#viewerId == null ? 'guest' : #viewerId) + '::' + (#viewerRole == null ? 'guest' : #viewerRole)")
    public long getPostCount(String authorId, String viewerId, String viewerRole) {
        return getPostsByUser(authorId, viewerId, viewerRole).size();
    }

    private Post requireActivePost(String postId) {
        return postRepository.findByPostIdAndDeletedFalse(postId.trim())
                .orElseThrow(() -> new NotFoundException("Post not found."));
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    private void publishSearchIndex(Post post, String operation) {
        try {
            searchIndexEventPublisher.publish(new SearchIndexEvent(post.getPostId(), normalizeContent(post.getContent()), operation));
        } catch (RuntimeException ignored) {
            // Search indexing is important for discovery, but posting itself should still succeed if the broker is temporarily unavailable.
        }
    }

    private void clearPostCaches() {
        for (String cacheName : List.of("postById", "postsByUser", "postFeeds", "postSearch", "postCounts")) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private boolean canViewPost(Post post, String viewerId, String viewerRole, Map<String, Boolean> followCache) {
        if (post.getVisibility() == PostVisibility.PUBLIC) {
            return true;
        }
        if (isAdmin(viewerRole)) {
            return true;
        }
        if (viewerId == null || viewerId.isBlank()) {
            return false;
        }
        String normalizedViewer = viewerId.trim();
        if (post.getAuthorId().equalsIgnoreCase(normalizedViewer)) {
            return true;
        }
        if (post.getVisibility() == PostVisibility.FOLLOWERS_ONLY) {
            String authorKey = post.getAuthorId().trim().toLowerCase(Locale.ROOT);
            return followCache.computeIfAbsent(authorKey, ignored -> isFollowing(normalizedViewer, post.getAuthorId()));
        }
        return false;
    }

    private boolean isAdmin(String viewerRole) {
        if (viewerRole == null) {
            return false;
        }
        String normalized = viewerRole.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN");
    }

    private boolean isFollowing(String followerId, String followeeId) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri(followServiceBaseUrl + "/api/v1/follows/is-following?followerId={followerId}&followeeId={followeeId}",
                            followerId, followeeId)
                    .retrieve()
                    .body(Map.class);
            Object value = response == null ? null : response.get("following");
            return value instanceof Boolean following && following;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void ensureCanModify(Post post, String actorId, String actorRole) {
        if (isAdmin(actorRole)) {
            return;
        }
        if (actorId == null || actorId.isBlank()) {
            throw new NotFoundException("Post not found.");
        }
        if (!post.getAuthorId().equalsIgnoreCase(actorId.trim())) {
            throw new NotFoundException("Post not found.");
        }
    }

    // This keeps post payloads consistent with the declared post type and prevents ambiguous combinations.
    private void validatePostShape(String content, List<String> mediaUrls, PostType postType) {
        String normalizedContent = normalizeContent(content);
        boolean hasContent = !normalizedContent.isBlank();
        boolean hasMedia = mediaUrls != null && mediaUrls.stream().anyMatch(url -> url != null && !url.trim().isBlank());

        if (!hasContent && !hasMedia) {
            throw new BadRequestException("A post must contain text, media, or both.");
        }

        switch (postType) {
            case TEXT_ONLY -> {
                if (!hasContent || hasMedia) {
                    throw new BadRequestException("TEXT_ONLY posts must contain text and no media.");
                }
            }
            case MEDIA_ONLY -> {
                if (hasContent || !hasMedia) {
                    throw new BadRequestException("MEDIA_ONLY posts must contain media and no text.");
                }
            }
            case TEXT_AND_MEDIA -> {
                if (!hasContent || !hasMedia) {
                    throw new BadRequestException("TEXT_AND_MEDIA posts must contain both text and media.");
                }
            }
        }
    }
}
