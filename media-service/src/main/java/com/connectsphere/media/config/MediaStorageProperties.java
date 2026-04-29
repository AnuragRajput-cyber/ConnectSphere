package com.connectsphere.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record MediaStorageProperties(
        String provider,
        String localDirectory,
        S3Properties s3
) {
    public record S3Properties(
            String bucket,
            String region,
            String keyPrefix,
            String publicBaseUrl
    ) {
    }
}
