package com.connectsphere.media.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.connectsphere.media.config.MediaStorageProperties;
import com.connectsphere.media.exception.BadRequestException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")
public class AzureBlobMediaStorageService implements MediaStorageService {

    private final BlobContainerClient containerClient;
    private final MediaStorageProperties properties;

    public AzureBlobMediaStorageService(MediaStorageProperties properties) {
        this.properties = properties;
        MediaStorageProperties.AzureProperties azure = properties.azure();
        String connectionString = azure == null ? null : azure.connectionString();
        String container = azure == null ? null : azure.container();

        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                    "AZURE_STORAGE_CONNECTION_STRING is required when MEDIA_STORAGE_PROVIDER=azure.");
        }
        if (container == null || container.isBlank()) {
            throw new IllegalStateException(
                    "AZURE_STORAGE_CONTAINER is required when MEDIA_STORAGE_PROVIDER=azure.");
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(container);
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    @Override
    public StoredMediaAsset store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A media file is required.");
        }

        String extension = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                : "";
        String prefix = properties.azure().keyPrefix() == null ? "" : properties.azure().keyPrefix().trim();
        String key = (prefix.isBlank() ? "" : prefix + "/") + UUID.randomUUID() + extension;

        BlobClient blobClient = containerClient.getBlobClient(key);
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        if (file.getContentType() != null && !file.getContentType().isBlank()) {
            blobClient.setHttpHeaders(new com.azure.storage.blob.models.BlobHttpHeaders()
                    .setContentType(file.getContentType()));
        }
        return new StoredMediaAsset(key, resolvePublicUrl(key));
    }

    @Override
    public boolean supportsLocalReads() {
        return false;
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        throw new UnsupportedOperationException("Azure Blob-backed media is served from its public URL.");
    }

    private String resolvePublicUrl(String key) {
        String publicBaseUrl = properties.azure().publicBaseUrl();
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + encodePath(key);
        }

        String accountName = properties.azure().accountName();
        String container = properties.azure().container();
        if (accountName != null && !accountName.isBlank() && container != null && !container.isBlank()) {
            return "https://" + accountName + ".blob.core.windows.net/" + container + "/" + encodePath(key);
        }

        return containerClient.getBlobContainerUrl().replaceAll("/+$", "") + "/" + encodePath(key);
    }

    private String encodePath(String value) {
        return String.join("/", Arrays.stream(value.split("/"))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .toList());
    }
}
