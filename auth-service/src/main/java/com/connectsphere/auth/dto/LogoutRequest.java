package com.connectsphere.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank String accessToken,
        String refreshToken
) {
}
