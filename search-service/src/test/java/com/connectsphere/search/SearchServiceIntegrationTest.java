package com.connectsphere.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.search.repository.HashtagRepository;
import com.connectsphere.search.repository.PostHashtagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SearchServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @BeforeEach
    void cleanIndex() {
        postHashtagRepository.deleteAll();
        hashtagRepository.deleteAll();
    }

    @Test
    void indexTrendingAndRemoveWork() throws Exception {
        mockMvc.perform(post("/api/v1/search/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": "post-1",
                                  "content": "Loving the #SpringBoot and #Java life"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hashtags[0]").value("springboot"));

        mockMvc.perform(get("/api/v1/hashtags/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[*].tag").value(org.hamcrest.Matchers.containsInAnyOrder("springboot", "java")));

        mockMvc.perform(delete("/api/v1/search/index/post-1"))
                .andExpect(status().isOk());
    }
}
