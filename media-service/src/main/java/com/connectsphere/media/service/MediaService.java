package com.connectsphere.media.service;

import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaResponse uploadMedia(String uploaderId, String linkedPostId, MultipartFile file) throws IOException;

    List<MediaResponse> getMediaByPost(String postId);

    MediaResponse getMediaById(String mediaId);

    void deleteMedia(String mediaId);

    StoryResponse createStory(String authorId, String caption, MultipartFile file) throws IOException;

    List<StoryResponse> getActiveStories(List<String> authorIds);

    StoryResponse getStoryById(String storyId);

    StoryResponse viewStory(String storyId);

    void deleteStory(String storyId);

    List<StoryResponse> getStoriesByUser(String authorId);

    void expireOldStories();
}
