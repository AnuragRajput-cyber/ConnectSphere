package com.connectsphere.media.storage;

import com.connectsphere.media.config.MediaStorageProperties;
import com.connectsphere.media.exception.BadRequestException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3MediaStorageService implements MediaStorageService {

    private final S3Client s3Client;
    private final MediaStorageProperties properties;

    public S3MediaStorageService(MediaStorageProperties properties) {
        this.properties = properties;
        String region = properties.s3() == null ? null : properties.s3().region();
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("AWS_REGION is required when MEDIA_STORAGE_PROVIDER=s3.");
        }
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    @Override
    public StoredMediaAsset store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A media file is required.");
        }
        String bucket = properties.s3() == null ? null : properties.s3().bucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET is required when MEDIA_STORAGE_PROVIDER=s3.");
        }

        String extension = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                : "";
        String prefix = properties.s3().keyPrefix() == null ? "" : properties.s3().keyPrefix().trim();
        String key = (prefix.isBlank() ? "" : prefix + "/") + UUID.randomUUID() + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        return new StoredMediaAsset(key, resolvePublicUrl(bucket, key));
    }

    @Override
    public boolean supportsLocalReads() {
        return false;
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        throw new UnsupportedOperationException("S3-backed media is served from its public URL.");
    }

    private String resolvePublicUrl(String bucket, String key) {
        String publicBaseUrl = properties.s3().publicBaseUrl();
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + key;
        }

        String region = Objects.requireNonNull(properties.s3().region(), "S3 region is required.");
        return URI.create("https://" + bucket + ".s3." + region + ".amazonaws.com/" + encodePath(key)).toString();
    }

    private String encodePath(String value) {
        return String.join("/", java.util.Arrays.stream(value.split("/"))
                .map(segment -> java.net.URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .toList());
    }
}
