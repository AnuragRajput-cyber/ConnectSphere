package com.connectsphere.media.service;

import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;
import com.connectsphere.media.entity.Media;
import com.connectsphere.media.entity.MediaType;
import com.connectsphere.media.entity.Story;
import com.connectsphere.media.exception.BadRequestException;
import com.connectsphere.media.exception.NotFoundException;
import com.connectsphere.media.repository.MediaRepository;
import com.connectsphere.media.repository.StoryRepository;
import com.connectsphere.media.storage.MediaStorageService;
import com.connectsphere.media.storage.StoredMediaAsset;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final StoryRepository storyRepository;
    private final MediaStorageService mediaStorageService;

    public MediaServiceImpl(
            MediaRepository mediaRepository,
            StoryRepository storyRepository,
            MediaStorageService mediaStorageService
    ) {
        this.mediaRepository = mediaRepository;
        this.storyRepository = storyRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    public MediaResponse uploadMedia(String uploaderId, String linkedPostId, MultipartFile file) throws IOException {
        MediaType mediaType = resolveMediaType(file.getContentType());
        StoredMediaAsset storedMediaAsset = mediaStorageService.store(file);

        Media media = new Media();
        media.setUploaderId(uploaderId.trim());
        media.setLinkedPostId(linkedPostId == null || linkedPostId.isBlank() ? null : linkedPostId.trim());
        media.setMediaType(mediaType);
        media.setMimeType(file.getContentType());
        media.setSizeKb(Math.max(1, file.getSize() / 1024));
        media.setUrl(storedMediaAsset.publicUrl());
        return MediaResponse.from(mediaRepository.save(media));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getMediaByPost(String postId) {
        return mediaRepository.findByLinkedPostIdAndDeletedFalse(postId.trim()).stream().map(MediaResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MediaResponse getMediaById(String mediaId) {
        return MediaResponse.from(getActiveMedia(mediaId));
    }

    @Override
    public void deleteMedia(String mediaId) {
        Media media = getActiveMedia(mediaId);
        media.setDeleted(true);
        mediaRepository.save(media);
    }

    @Override
    public StoryResponse createStory(String authorId, String caption, MultipartFile file) throws IOException {
        StoredMediaAsset storedMediaAsset = mediaStorageService.store(file);
        Story story = new Story();
        story.setAuthorId(authorId.trim());
        story.setCaption(caption == null || caption.isBlank() ? null : caption.trim());
        story.setMediaType(resolveMediaType(file.getContentType()));
        story.setMediaUrl(storedMediaAsset.publicUrl());
        story.setViewsCount(0);
        return StoryResponse.from(storyRepository.save(story));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoryResponse> getActiveStories(List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return storyRepository.findAll().stream()
                    .filter(Story::isActive)
                    .map(StoryResponse::from)
                    .toList();
        }
        return storyRepository.findByAuthorIdInAndActiveTrueOrderByCreatedAtDesc(authorIds).stream()
                .map(StoryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponse getStoryById(String storyId) {
        Story story = storyRepository.findById(storyId.trim())
                .orElseThrow(() -> new NotFoundException("Story not found."));
        return StoryResponse.from(story);
    }

    @Override
    public StoryResponse viewStory(String storyId) {
        Story story = getActiveStory(storyId);
        story.setViewsCount(story.getViewsCount() + 1);
        return StoryResponse.from(storyRepository.save(story));
    }

    @Override
    public void deleteStory(String storyId) {
        Story story = getActiveStory(storyId);
        story.setActive(false);
        storyRepository.save(story);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoryResponse> getStoriesByUser(String authorId) {
        return storyRepository.findByAuthorIdAndActiveTrueOrderByCreatedAtDesc(authorId.trim()).stream()
                .map(StoryResponse::from)
                .toList();
    }

    @Override
    @Scheduled(fixedDelay = 300000)
    public void expireOldStories() {
        storyRepository.findByExpiresAtBeforeAndActiveTrue(Instant.now()).forEach(story -> story.setActive(false));
    }

    private Media getActiveMedia(String mediaId) {
        return mediaRepository.findByMediaIdAndDeletedFalse(mediaId.trim())
                .orElseThrow(() -> new NotFoundException("Media not found."));
    }

    private Story getActiveStory(String storyId) {
        return storyRepository.findByStoryIdAndActiveTrue(storyId.trim())
                .orElseThrow(() -> new NotFoundException("Story not found."));
    }

    private MediaType resolveMediaType(String mimeType) {
        if (mimeType == null) {
            throw new BadRequestException("Unsupported media type.");
        }
        if ("image/jpeg".equalsIgnoreCase(mimeType)
                || "image/png".equalsIgnoreCase(mimeType)
                || "image/webp".equalsIgnoreCase(mimeType)) {
            return MediaType.IMAGE;
        }
        if ("video/mp4".equalsIgnoreCase(mimeType)) {
            return MediaType.VIDEO;
        }
        throw new BadRequestException("Only JPEG, PNG, WebP images and MP4 videos are supported.");
    }
}
