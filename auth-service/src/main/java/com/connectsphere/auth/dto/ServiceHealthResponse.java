package com.connectsphere.auth.dto;

public record ServiceHealthResponse(
        String service,
        String status,
        String baseUrl
) {
}
