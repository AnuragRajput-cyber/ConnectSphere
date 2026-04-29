package com.connectsphere.media.controller;

import com.connectsphere.media.dto.ApiMessageResponse;
import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;
import com.connectsphere.media.service.MediaService;
import com.connectsphere.media.storage.MediaStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/v1", ""})
@Tag(name = "Media Service", description = "Media upload and 24-hour story endpoints.")
public class MediaResource {

    private final MediaService mediaService;
    private final MediaStorageService mediaStorageService;

    public MediaResource(MediaService mediaService, MediaStorageService mediaStorageService) {
        this.mediaService = mediaService;
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media")
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam String uploaderId,
            @RequestParam(required = false) String linkedPostId,
            @RequestParam MultipartFile file,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) throws IOException {
        if (!isAdmin(actorRole) && !uploaderId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(mediaService.uploadMedia(uploaderId, linkedPostId, file));
    }

    @GetMapping("/media/post/{postId}")
    public ResponseEntity<List<MediaResponse>> getMediaByPost(@PathVariable String postId) {
        return ResponseEntity.ok(mediaService.getMediaByPost(postId));
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable String mediaId) {
        return ResponseEntity.ok(mediaService.getMediaById(mediaId));
    }

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<ApiMessageResponse> deleteMedia(@PathVariable String mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.ok(new ApiMessageResponse("Media soft-deleted successfully."));
    }

    @PostMapping(value = "/stories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoryResponse> createStory(
            @RequestParam String authorId,
            @RequestParam(required = false) String caption,
            @RequestParam MultipartFile file,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) throws IOException {
        if (!isAdmin(actorRole) && !authorId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(mediaService.createStory(authorId, caption, file));
    }

    private boolean isAdmin(String role) {
        return role != null && (role.trim().equalsIgnoreCase("ADMIN") || role.trim().equalsIgnoreCase("ROLE_ADMIN"));
    }

    @GetMapping("/stories/active")
    public ResponseEntity<List<StoryResponse>> getActiveStories(@RequestParam(required = false) List<String> authorIds) {
        return ResponseEntity.ok(mediaService.getActiveStories(authorIds));
    }

    @GetMapping("/stories/{storyId}")
    @Operation(summary = "Get one story")
    public ResponseEntity<StoryResponse> getStoryById(@PathVariable String storyId) {
        return ResponseEntity.ok(mediaService.getStoryById(storyId));
    }

    @PostMapping("/stories/{storyId}/view")
    public ResponseEntity<StoryResponse> viewStory(@PathVariable String storyId) {
        return ResponseEntity.ok(mediaService.viewStory(storyId));
    }

    @DeleteMapping("/stories/{storyId}")
    public ResponseEntity<ApiMessageResponse> deleteStory(@PathVariable String storyId) {
        mediaService.deleteStory(storyId);
        return ResponseEntity.ok(new ApiMessageResponse("Story deleted successfully."));
    }

    @GetMapping("/stories/user/{authorId}")
    public ResponseEntity<List<StoryResponse>> getStoriesByUser(@PathVariable String authorId) {
        return ResponseEntity.ok(mediaService.getStoriesByUser(authorId));
    }

    @GetMapping("/media/files/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        if (!mediaStorageService.supportsLocalReads()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = mediaStorageService.loadAsResource(filename);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(resource);
    }
}
