package com.connectsphere.gateway.security;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(
            WebClient.Builder builder,
            @Value("${app.services.auth-base-url:http://localhost:8081}") String authBaseUrl
    ) {
        this.webClient = builder.baseUrl(authBaseUrl).build();
    }

    public Mono<TokenValidationResponse> validateToken(String accessToken) {
        return webClient.post()
                .uri("/api/v1/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TokenValidationRequest(accessToken))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .onErrorReturn(new TokenValidationResponse(false, null, null, null, null));
    }

    public record TokenValidationRequest(String token) {
    }

    public record TokenValidationResponse(
            boolean valid,
            String userId,
            String email,
            String role,
            Instant expiresAt
    ) {
    }
}

