package com.connectsphere.follow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.follow.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FollowResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void cleanFollows() {
        followRepository.deleteAll();
    }

    @Test
    void followSuggestionAndUnfollowFlowWorks() throws Exception {
        mockMvc.perform(post("/api/v1/follows")
                        .header("X-User-Id", "user-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"followerId":"user-a","followeeId":"user-b","status":"ACTIVE"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/follows")
                        .header("X-User-Id", "user-b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"followerId":"user-b","followeeId":"user-c","status":"ACTIVE"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/follows/suggested/user-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("user-c"));

        mockMvc.perform(delete("/api/v1/follows")
                        .header("X-User-Id", "user-a")
                        .param("followerId", "user-a")
                        .param("followeeId", "user-b"))
                .andExpect(status().isOk());
    }
}
