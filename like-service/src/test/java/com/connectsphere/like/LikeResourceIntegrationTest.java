package com.connectsphere.like;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.like.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LikeResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LikeRepository likeRepository;

    @BeforeEach
    void cleanLikes() {
        likeRepository.deleteAll();
    }

    @Test
    void likeChangeReactionAndUnlike() throws Exception {
        mockMvc.perform(post("/api/v1/likes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "targetId": "post-1",
                                  "targetType": "POST",
                                  "reactionType": "LIKE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reactionType").value("LIKE"));

        mockMvc.perform(put("/api/v1/likes/user-1/post-1")
                        .header("X-User-Id", "user-1")
                        .param("targetType", "POST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reactionType": "LOVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reactionType").value("LOVE"));

        mockMvc.perform(get("/api/v1/likes/summary/post-1").param("targetType", "POST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LOVE").value(1));

        mockMvc.perform(delete("/api/v1/likes")
                        .header("X-User-Id", "user-1")
                        .param("userId", "user-1")
                        .param("targetId", "post-1")
                        .param("targetType", "POST"))
                .andExpect(status().isOk());
    }
}
