package com.connectsphere.auth.service;

import com.connectsphere.auth.dto.AdminPlatformOverviewResponse;
import com.connectsphere.auth.dto.AdminStatsResponse;
import com.connectsphere.auth.dto.AdminSystemOverviewResponse;
import com.connectsphere.auth.dto.HashtagSummaryResponse;
import com.connectsphere.auth.dto.ServiceHealthResponse;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AdminInsightsService {

    private final UserRepository userRepository;
    private final RestClient restClient;
    private final String postServiceBaseUrl;
    private final String commentServiceBaseUrl;
    private final String likeServiceBaseUrl;
    private final String followServiceBaseUrl;
    private final String notificationServiceBaseUrl;
    private final String mediaServiceBaseUrl;
    private final String searchServiceBaseUrl;
    private final String chatServiceBaseUrl;
    private final String gatewayBaseUrl;

    public AdminInsightsService(
            UserRepository userRepository,
            @Value("${app.services.post-base-url:http://localhost:8082}") String postServiceBaseUrl,
            @Value("${app.services.comment-base-url:http://localhost:8083}") String commentServiceBaseUrl,
            @Value("${app.services.like-base-url:http://localhost:8084}") String likeServiceBaseUrl,
            @Value("${app.services.follow-base-url:http://localhost:8085}") String followServiceBaseUrl,
            @Value("${app.services.notification-base-url:http://localhost:8086}") String notificationServiceBaseUrl,
            @Value("${app.services.media-base-url:http://localhost:8087}") String mediaServiceBaseUrl,
            @Value("${app.services.search-base-url:http://localhost:8088}") String searchServiceBaseUrl,
            @Value("${app.services.chat-base-url:http://localhost:8089}") String chatServiceBaseUrl,
            @Value("${app.services.gateway-base-url:http://localhost:8080}") String gatewayBaseUrl
    ) {
        this.userRepository = userRepository;
        this.restClient = RestClient.builder().build();
        this.postServiceBaseUrl = postServiceBaseUrl;
        this.commentServiceBaseUrl = commentServiceBaseUrl;
        this.likeServiceBaseUrl = likeServiceBaseUrl;
        this.followServiceBaseUrl = followServiceBaseUrl;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
        this.mediaServiceBaseUrl = mediaServiceBaseUrl;
        this.searchServiceBaseUrl = searchServiceBaseUrl;
        this.chatServiceBaseUrl = chatServiceBaseUrl;
        this.gatewayBaseUrl = gatewayBaseUrl;
    }

    public AdminStatsResponse buildUserStats() {
        List<User> users = userRepository.findAll();
        long total = users.size();
        long active = users.stream().filter(User::isActive).count();
        long inactive = total - active;
        long verified = users.stream().filter(User::isEmailVerified).count();
        long admins = users.stream().filter(user -> user.getRole() == Role.ADMIN).count();
        return new AdminStatsResponse(total, active, inactive, verified, admins);
    }

    public AdminPlatformOverviewResponse buildPlatformOverview() {
        List<HashtagSummaryResponse> trendingHashtags = fetchTrendingHashtags();
        return new AdminPlatformOverviewResponse(buildUserStats(), trendingHashtags);
    }

    public AdminSystemOverviewResponse buildSystemOverview() {
        return new AdminSystemOverviewResponse(List.of(
                probe("api-gateway", gatewayBaseUrl),
                probe("auth-service", "http://localhost:8081"),
                probe("post-service", postServiceBaseUrl),
                probe("comment-service", commentServiceBaseUrl),
                probe("like-service", likeServiceBaseUrl),
                probe("follow-service", followServiceBaseUrl),
                probe("notification-service", notificationServiceBaseUrl),
                probe("media-service", mediaServiceBaseUrl),
                probe("search-service", searchServiceBaseUrl),
                probe("chat-service", chatServiceBaseUrl)
        ));
    }

    private List<HashtagSummaryResponse> fetchTrendingHashtags() {
        try {
            List<HashtagSummaryResponse> response = restClient.get()
                    .uri(searchServiceBaseUrl + "/api/v1/hashtags/trending")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<HashtagSummaryResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private ServiceHealthResponse probe(String service, String baseUrl) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(baseUrl + "/actuator/health")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            String status = response == null ? "UNKNOWN" : String.valueOf(response.getOrDefault("status", "UNKNOWN"));
            return new ServiceHealthResponse(service, status, baseUrl);
        } catch (RestClientResponseException exception) {
            HttpStatusCode statusCode = exception.getStatusCode();
            return new ServiceHealthResponse(service, "HTTP_" + statusCode.value(), baseUrl);
        } catch (RuntimeException ignored) {
            return new ServiceHealthResponse(service, "DOWN", baseUrl);
        }
    }
}
