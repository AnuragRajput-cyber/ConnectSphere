package com.connectsphere.media;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.connectsphere.media.repository.MediaRepository;
import com.connectsphere.media.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MediaResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private StoryRepository storyRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanMediaAndStories() {
        storyRepository.deleteAll();
        mediaRepository.deleteAll();
    }

    @Test
    void uploadMediaAndStoryFlowWorks() throws Exception {
        MockMultipartFile image = new MockMultipartFile("file", "cover.png", "image/png", "png".getBytes());

        String mediaJson = mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(image)
                        .header("X-User-Id", "user-1")
                        .param("uploaderId", "user-1")
                        .param("linkedPostId", "post-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("IMAGE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String mediaId = objectMapper.readTree(mediaJson).get("mediaId").asText();

        mockMvc.perform(get("/api/v1/media/post/post-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mediaId").value(mediaId));

        String storyJson = mockMvc.perform(multipart("/api/v1/stories")
                        .file(new MockMultipartFile("file", "story.jpg", "image/jpeg", "story".getBytes()))
                        .header("X-User-Id", "user-1")
                        .param("authorId", "user-1")
                        .param("caption", "Story time"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode storyNode = objectMapper.readTree(storyJson);
        String storyId = storyNode.get("storyId").asText();

        mockMvc.perform(get("/api/v1/stories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/v1/stories/active")
                        .param("authorIds", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storyId").value(storyId));

        mockMvc.perform(post("/api/v1/stories/{storyId}/view", storyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewsCount").value(1));

        mockMvc.perform(delete("/api/v1/media/{mediaId}", mediaId)
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }
}
