package com.connectsphere.chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.chat.repository.ChatMessageRepository;
import com.connectsphere.chat.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChatRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @BeforeEach
    void cleanChatData() {
        chatMessageRepository.deleteAll();
        conversationRepository.deleteAll();
    }

    @Test
    void conversationAndMessageHistoryWork() throws Exception {
        String conversation = mockMvc.perform(post("/api/v1/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"participantOneId":"user-1","participantTwoId":"user-2"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String conversationId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(conversation).get("conversationId").asText();

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conversationId":"%s",
                                  "senderId":"user-1",
                                  "recipientId":"user-2",
                                  "content":"Hello there"
                                }
                                """.formatted(conversationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello there"));

        mockMvc.perform(get("/api/v1/chat/messages/{conversationId}", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value(conversationId));

        mockMvc.perform(delete("/api/v1/chat/messages/{conversationId}", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conversation messages cleared."));

        mockMvc.perform(get("/api/v1/chat/messages/{conversationId}", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
