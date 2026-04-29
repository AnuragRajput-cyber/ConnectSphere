package com.connectsphere.auth.dto;

import java.time.Instant;

public record RegisterPendingResponse(
        String userId,
        String email,
        Instant otpExpiresAt,
        String debugOtpCode
) {
}

