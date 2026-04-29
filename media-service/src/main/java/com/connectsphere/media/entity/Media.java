package com.connectsphere.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media")
public class Media {

    @Id
    @Column(name = "media_id", nullable = false, updatable = false, length = 36)
    private String mediaId;

    @Column(name = "uploader_id", nullable = false, length = 36)
    private String uploaderId;

    @Column(nullable = false, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Column(name = "size_kb", nullable = false)
    private long sizeKb;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "linked_post_id", length = 36)
    private String linkedPostId;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @PrePersist
    void onCreate() {
        if (mediaId == null) {
            mediaId = UUID.randomUUID().toString();
        }
        uploadedAt = Instant.now();
        deleted = false;
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public long getSizeKb() {
        return sizeKb;
    }

    public void setSizeKb(long sizeKb) {
        this.sizeKb = sizeKb;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getLinkedPostId() {
        return linkedPostId;
    }

    public void setLinkedPostId(String linkedPostId) {
        this.linkedPostId = linkedPostId;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
