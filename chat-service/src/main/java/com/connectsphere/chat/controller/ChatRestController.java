package com.connectsphere.chat.controller;

import com.connectsphere.chat.dto.ChatMessageRequest;
import com.connectsphere.chat.dto.ChatMessageResponse;
import com.connectsphere.chat.dto.ConversationRequest;
import com.connectsphere.chat.dto.ConversationResponse;
import java.util.Map;
import com.connectsphere.chat.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/chat", "/chat"})
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> createConversation(@Valid @RequestBody ConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createOrGetConversation(request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(@RequestParam String userId) {
        return ResponseEntity.ok(chatService.getConversations(userId));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> saveMessage(@Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.saveMessage(request));
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }

    @DeleteMapping("/messages/{conversationId}")
    public ResponseEntity<Map<String, String>> clearMessages(@PathVariable String conversationId) {
        chatService.clearMessages(conversationId);
        return ResponseEntity.ok(Map.of("message", "Conversation messages cleared."));
    }
}
