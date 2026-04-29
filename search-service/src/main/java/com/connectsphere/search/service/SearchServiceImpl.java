package com.connectsphere.search.service;

import com.connectsphere.search.document.HashtagSearchDocument;
import com.connectsphere.search.document.PostSearchDocument;
import com.connectsphere.search.dto.HashtagResponse;
import com.connectsphere.search.entity.Hashtag;
import com.connectsphere.search.entity.PostHashtag;
import com.connectsphere.search.repository.HashtagRepository;
import com.connectsphere.search.repository.HashtagSearchRepository;
import com.connectsphere.search.repository.PostHashtagRepository;
import com.connectsphere.search.repository.PostSearchRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([A-Za-z0-9_]{1,50})");

    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostSearchRepository postSearchRepository;
    private final HashtagSearchRepository hashtagSearchRepository;
    private final RestClient restClient;
    private final String authServiceBaseUrl;
    private final String postServiceBaseUrl;
    private final String searchProvider;

    public SearchServiceImpl(
            HashtagRepository hashtagRepository,
            PostHashtagRepository postHashtagRepository,
            ObjectProvider<PostSearchRepository> postSearchRepositoryProvider,
            ObjectProvider<HashtagSearchRepository> hashtagSearchRepositoryProvider,
            @Value("${app.services.auth-base-url:http://localhost:8081}") String authServiceBaseUrl,
            @Value("${app.services.post-base-url:http://localhost:8082}") String postServiceBaseUrl,
            @Value("${app.search.provider:database}") String searchProvider
    ) {
        this.hashtagRepository = hashtagRepository;
        this.postHashtagRepository = postHashtagRepository;
        this.postSearchRepository = postSearchRepositoryProvider.getIfAvailable();
        this.hashtagSearchRepository = hashtagSearchRepositoryProvider.getIfAvailable();
        this.authServiceBaseUrl = authServiceBaseUrl;
        this.postServiceBaseUrl = postServiceBaseUrl;
        this.searchProvider = searchProvider == null ? "database" : searchProvider.trim().toLowerCase(Locale.ROOT);
        this.restClient = RestClient.builder().build();
    }

    @Override
    public List<String> indexPost(String postId, String content) {
        removePostIndex(postId);
        LinkedHashSet<String> tags = extractTags(content);
        for (String tag : tags) {
            Hashtag hashtag = hashtagRepository.findByTag(tag)
                    .orElseGet(() -> {
                        Hashtag created = new Hashtag();
                        created.setTag(tag);
                        created.setPostCount(0);
                        return created;
                    });
            hashtag.setPostCount(hashtag.getPostCount() + 1);
            hashtag.setLastUsedAt(Instant.now());
            Hashtag savedHashtag = hashtagRepository.save(hashtag);
            syncHashtagDocument(savedHashtag);

            PostHashtag mapping = new PostHashtag();
            mapping.setPostId(postId.trim());
            mapping.setHashtagId(savedHashtag.getHashtagId());
            postHashtagRepository.save(mapping);
        }
        syncPostDocument(postId.trim(), normalizeContent(content), tags.stream().toList());
        return tags.stream().toList();
    }

    @Override
    public void removePostIndex(String postId) {
        for (PostHashtag mapping : postHashtagRepository.findByPostId(postId.trim())) {
            hashtagRepository.findById(mapping.getHashtagId()).ifPresent(hashtag -> {
                hashtag.setPostCount(Math.max(0, hashtag.getPostCount() - 1));
                Hashtag saved = hashtagRepository.save(hashtag);
                syncHashtagDocument(saved);
            });
        }
        postHashtagRepository.deleteByPostId(postId.trim());
        if (useElasticsearch()) {
            postSearchRepository.deleteById(postId.trim());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object searchPosts(String query, String viewerId, String viewerRole) {
        if (useElasticsearch()) {
            return hydratePosts(searchPostIds(query), viewerId, viewerRole);
        }
        return restClient.get()
                .uri(postServiceBaseUrl + "/api/v1/posts/search?query={query}", query)
                .headers(headers -> applyViewerHeaders(headers, viewerId, viewerRole))
                .retrieve()
                .body(Object.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Object searchUsers(String query) {
        return restClient.get()
                .uri(authServiceBaseUrl + "/api/v1/auth/search?query={query}", query)
                .retrieve()
                .body(Object.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HashtagResponse> getHashtagsForPost(String postId) {
        return postHashtagRepository.findByPostId(postId.trim()).stream()
                .map(mapping -> hashtagRepository.findById(mapping.getHashtagId()).orElse(null))
                .filter(Objects::nonNull)
                .map(HashtagResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HashtagResponse> getTrendingHashtags() {
        if (useElasticsearch()) {
            return hashtagSearchRepository.findTop10ByOrderByPostCountDescLastUsedAtDesc().stream()
                    .map(document -> new HashtagResponse(document.hashtagId(), document.tag(), document.postCount(), document.lastUsedAt()))
                    .toList();
        }
        return hashtagRepository.findTop10ByOrderByPostCountDescLastUsedAtDesc().stream()
                .map(HashtagResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPostsByHashtag(String tag) {
        if (useElasticsearch()) {
            return postSearchRepository.findTop50ByHashtagsContainingOrderByUpdatedAtDesc(normalizeTag(tag)).stream()
                    .map(PostSearchDocument::postId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        return hashtagRepository.findByTag(normalizeTag(tag))
                .map(hashtag -> postHashtagRepository.findByHashtagId(hashtag.getHashtagId()).stream().map(PostHashtag::getPostId).toList())
                .orElseGet(List::of);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HashtagResponse> searchHashtags(String query) {
        if (useElasticsearch()) {
            return hashtagSearchRepository.findTop20ByTagContainingIgnoreCaseOrderByPostCountDesc(normalizeTag(query)).stream()
                    .map(document -> new HashtagResponse(document.hashtagId(), document.tag(), document.postCount(), document.lastUsedAt()))
                    .toList();
        }
        return hashtagRepository.findByTagContainingIgnoreCaseOrderByPostCountDesc(normalizeTag(query)).stream()
                .map(HashtagResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getHashtagCount(String tag) {
        return hashtagRepository.findByTag(normalizeTag(tag))
                .map(hashtag -> postHashtagRepository.countByHashtagId(hashtag.getHashtagId()))
                .orElse(0L);
    }

    private LinkedHashSet<String> extractTags(String content) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }
        return tags;
    }

    private String normalizeTag(String tag) {
        String normalized = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("#") ? normalized.substring(1) : normalized;
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    private boolean useElasticsearch() {
        return "elasticsearch".equals(searchProvider)
                && postSearchRepository != null
                && hashtagSearchRepository != null;
    }

    private void syncHashtagDocument(Hashtag hashtag) {
        if (!useElasticsearch()) {
            return;
        }
        if (hashtag.getPostCount() <= 0) {
            hashtagSearchRepository.deleteById(hashtag.getHashtagId());
            return;
        }
        hashtagSearchRepository.save(new HashtagSearchDocument(
                hashtag.getHashtagId(),
                hashtag.getTag(),
                hashtag.getPostCount(),
                hashtag.getLastUsedAt()
        ));
    }

    private void syncPostDocument(String postId, String content, List<String> hashtags) {
        if (!useElasticsearch()) {
            return;
        }
        postSearchRepository.save(new PostSearchDocument(postId, content, hashtags, Instant.now()));
    }

    private List<String> searchPostIds(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedTag = normalizeTag(query);
        return postSearchRepository
                .findTop50ByContentContainingIgnoreCaseOrHashtagsContainingOrderByUpdatedAtDesc(normalizedQuery, normalizedTag)
                .stream()
                .map(PostSearchDocument::postId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Object> hydratePosts(List<String> postIds, String viewerId, String viewerRole) {
        return postIds.stream()
                .map(postId -> fetchPost(postId, viewerId, viewerRole))
                .filter(Objects::nonNull)
                .toList();
    }

    private Object fetchPost(String postId, String viewerId, String viewerRole) {
        try {
            return restClient.get()
                    .uri(postServiceBaseUrl + "/api/v1/posts/{postId}", postId)
                    .headers(headers -> applyViewerHeaders(headers, viewerId, viewerRole))
                    .retrieve()
                    .body(Object.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private void applyViewerHeaders(org.springframework.http.HttpHeaders headers, String viewerId, String viewerRole) {
        if (viewerId != null && !viewerId.isBlank()) {
            headers.set("X-User-Id", viewerId);
        }
        if (viewerRole != null && !viewerRole.isBlank()) {
            headers.set("X-User-Role", viewerRole);
        }
    }
}
