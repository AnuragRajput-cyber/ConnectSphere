package com.connectsphere.gateway.security;

import java.util.List;
import java.util.Locale;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    private static final List<String> PUBLIC_POST_PREFIXES = List.of(
            "/api/v1/posts",
            "/posts",
            "/api/v1/comments",
            "/comments",
            "/api/v1/follows/followers",
            "/api/v1/follows/following",
            "/follows/followers",
            "/follows/following",
            "/api/v1/search",
            "/search",
            "/api/v1/hashtags",
            "/hashtags",
            "/api/v1/media/files",
            "/media/files",
            "/api/v1/stories/active",
            "/stories/active",
            "/api/v1/auth/search",
            "/auth/search",
            "/api/v1/auth/users/",
            "/auth/users/"
    );

    private static final List<String> PUBLIC_AUTH_PREFIXES = List.of(
            "/api/v1/auth/register",
            "/auth/register",
            "/api/v1/auth/verify-email",
            "/auth/verify-email",
            "/api/v1/auth/resend-otp",
            "/auth/resend-otp",
            "/api/v1/auth/forgot-password",
            "/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/auth/reset-password",
            "/api/v1/auth/login",
            "/auth/login",
            "/api/v1/auth/refresh",
            "/auth/refresh",
            "/api/v1/auth/validate",
            "/auth/validate",
            "/oauth2/",
            "/login/oauth2/"
    );

    private final AuthServiceClient authServiceClient;

    public GatewayAuthFilter(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Only enforce auth for API calls (frontend routes like /feed, /profile/* are handled by Angular guards).
        if (!path.startsWith("/api/") && !path.startsWith("/auth") && !path.startsWith("/posts") && !path.startsWith("/comments")
                && !path.startsWith("/search") && !path.startsWith("/hashtags") && !path.startsWith("/media") && !path.startsWith("/stories")
                && !path.startsWith("/oauth2") && !path.startsWith("/login/oauth2") && !path.startsWith("/ws")) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/ws")) {
            return chain.filter(exchange);
        }

        String token = extractBearerToken(exchange.getRequest().getHeaders());
        boolean publicRequest = isPublic(path, exchange.getRequest().getMethod());
        if (token == null && publicRequest) {
            return chain.filter(exchange);
        }
        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return authServiceClient.validateToken(token)
                .flatMap(validation -> {
                    if (!validation.valid() || validation.userId() == null) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header(HEADER_USER_ID, validation.userId())
                            .header(HEADER_USER_EMAIL, validation.email() == null ? "" : validation.email())
                            .header(HEADER_USER_ROLE, validation.role() == null ? "" : validation.role())
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                });
    }

    private boolean isPublic(String path, HttpMethod method) {
        String normalized = path.toLowerCase(Locale.ROOT);
        for (String prefix : PUBLIC_AUTH_PREFIXES) {
            if (normalized.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        if (method == HttpMethod.GET) {
            for (String prefix : PUBLIC_POST_PREFIXES) {
                if (normalized.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        }

        return false;
    }

    private String extractBearerToken(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() < 7) {
            return null;
        }
        if (!trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return trimmed.substring(7).trim();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
