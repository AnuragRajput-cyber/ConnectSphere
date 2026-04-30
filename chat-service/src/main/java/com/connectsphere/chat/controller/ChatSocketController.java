package com.connectsphere.chat.controller;

import com.connectsphere.chat.dto.ChatMessageRequest;
import com.connectsphere.chat.dto.ChatMessageResponse;
import com.connectsphere.chat.dto.TypingIndicator;
import com.connectsphere.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request) {
        ChatMessageResponse savedMessage = chatService.saveMessage(request);
        messagingTemplate.convertAndSend("/topic/chat." + savedMessage.conversationId(), savedMessage);
    }

    @MessageMapping("/chat.typing")
    public void sendTyping(@Payload TypingIndicator typingIndicator) {
        messagingTemplate.convertAndSend("/topic/chat.typing." + typingIndicator.conversationId(), typingIndicator);
    }
}
