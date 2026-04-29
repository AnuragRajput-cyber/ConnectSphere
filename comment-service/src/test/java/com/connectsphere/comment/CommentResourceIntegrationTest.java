package com.connectsphere.comment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommentResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void cleanComments() {
        commentRepository.deleteAll();
    }

    @Test
    void addReplyLikeAndCountComment() throws Exception {
        String created = mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": "post-1",
                                  "authorId": "user-1",
                                  "content": "Top-level comment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Top-level comment"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String commentId = JsonTestHelper.readField(created, "commentId");

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": "post-1",
                                  "authorId": "user-2",
                                  "parentCommentId": "%s",
                                  "content": "Nested reply"
                                }
                                """.formatted(commentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentCommentId").value(commentId));

        mockMvc.perform(post("/api/v1/comments/{commentId}/likes", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(1));

        mockMvc.perform(get("/api/v1/comments/count").param("postId", "post-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void updateAndDeleteComment() throws Exception {
        String created = mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": "post-2",
                                  "authorId": "user-1",
                                  "content": "Before update"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String commentId = JsonTestHelper.readField(created, "commentId");

        mockMvc.perform(put("/api/v1/comments/{commentId}", commentId)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "After update"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("After update"));

        mockMvc.perform(delete("/api/v1/comments/{commentId}", commentId)
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/comments/{commentId}", commentId))
                .andExpect(status().isNotFound());
    }
}
