package com.connectsphere.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        Duration ttl,
        int maxAttempts,
        boolean returnCodeInResponse
) {
    public OtpProperties {
        if (ttl == null) {
            ttl = Duration.ofMinutes(10);
        }
        if (maxAttempts <= 0) {
            maxAttempts = 5;
        }
    }
}

