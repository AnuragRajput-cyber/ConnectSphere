package com.connectsphere.media.dto;

import com.connectsphere.media.entity.Media;
import java.time.Instant;

public record MediaResponse(
        String mediaId,
        String uploaderId,
        String url,
        String mediaType,
        long sizeKb,
        String mimeType,
        String linkedPostId,
        boolean deleted,
        Instant uploadedAt
) {
    public static MediaResponse from(Media media) {
        return new MediaResponse(
                media.getMediaId(),
                media.getUploaderId(),
                media.getUrl(),
                media.getMediaType().name(),
                media.getSizeKb(),
                media.getMimeType(),
                media.getLinkedPostId(),
                media.isDeleted(),
                media.getUploadedAt()
        );
    }
}
