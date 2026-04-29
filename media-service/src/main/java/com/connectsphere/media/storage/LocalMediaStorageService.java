package com.connectsphere.media.storage;

import com.connectsphere.media.config.MediaStorageProperties;
import com.connectsphere.media.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalMediaStorageService implements MediaStorageService {

    private final Path storageDirectory;

    public LocalMediaStorageService(MediaStorageProperties properties) {
        this.storageDirectory = Path.of(properties.localDirectory() == null || properties.localDirectory().isBlank()
                ? "uploads"
                : properties.localDirectory());
    }

    @Override
    public StoredMediaAsset store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A media file is required.");
        }
        Files.createDirectories(storageDirectory);
        String extension = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + extension;
        Path target = storageDirectory.resolve(filename);
        Files.write(target, file.getBytes());
        return new StoredMediaAsset(filename, "/media/files/" + filename);
    }

    @Override
    public boolean supportsLocalReads() {
        return true;
    }

    @Override
    public Resource loadAsResource(String storageKey) throws IOException {
        Path file = storageDirectory.resolve(storageKey).normalize();
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) {
            throw new IOException("Stored media does not exist.");
        }
        return resource;
    }
}
