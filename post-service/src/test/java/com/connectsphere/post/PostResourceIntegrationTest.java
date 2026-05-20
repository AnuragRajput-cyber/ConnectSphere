package com.connectsphere.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PostResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void cleanPosts() {
        postRepository.deleteAll();
    }

    @Test
    void swaggerEndpointsAreAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("ConnectSphere Post Service API"))
                .andExpect(jsonPath("$.servers[0].url").value("/"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void createUpdateSearchAndCountPost() throws Exception {
        String created = mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorId": "user-1",
                                  "content": "My first public post",
                                  "mediaUrls": [],
                                  "postType": "TEXT_ONLY",
                                  "visibility": "PUBLIC"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId").value("user-1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = JsonTestHelper.readField(created, "postId");

        mockMvc.perform(put("/api/v1/posts/{postId}", postId)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "My edited public post",
                                  "mediaUrls": [],
                                  "postType": "TEXT_ONLY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("My edited public post"));

        mockMvc.perform(get("/api/v1/posts/search")
                        .param("query", "edited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(postId));

        mockMvc.perform(get("/api/v1/posts/count/{authorId}", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void feedVisibilityAndCountersWork() throws Exception {
        String postOne = mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "followee-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorId": "followee-1",
                                  "content": "Feed content",
                                  "mediaUrls": [],
                                  "postType": "TEXT_ONLY",
                                  "visibility": "PUBLIC"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = JsonTestHelper.readField(postOne, "postId");

        mockMvc.perform(post("/api/v1/posts/{postId}/likes/increment", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(1));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments/increment", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentsCount").value(1));

        mockMvc.perform(put("/api/v1/posts/{postId}/visibility", postId)
                        .header("X-User-Id", "followee-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "visibility": "FOLLOWERS_ONLY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("FOLLOWERS_ONLY"));

        mockMvc.perform(get("/api/v1/posts/feed")
                        .param("userIds", "followee-1", "followee-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(postId));
    }

    @Test
    void deleteSoftDeletesPost() throws Exception {
        String created = mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "user-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorId": "user-delete",
                                  "content": "",
                                  "mediaUrls": ["https://cdn.example.com/photo.png"],
                                  "postType": "MEDIA_ONLY",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = JsonTestHelper.readField(created, "postId");

        mockMvc.perform(delete("/api/v1/posts/{postId}", postId)
                        .header("X-User-Id", "user-delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post soft-deleted successfully."));

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isNotFound());
    }
}
