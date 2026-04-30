package com.connectsphere.chat.service;

import com.connectsphere.chat.dto.ChatMessageRequest;
import com.connectsphere.chat.dto.ChatMessageResponse;
import com.connectsphere.chat.dto.ConversationRequest;
import com.connectsphere.chat.dto.ConversationResponse;
import java.util.List;

public interface ChatService {

    ConversationResponse createOrGetConversation(ConversationRequest request);

    List<ConversationResponse> getConversations(String userId);

    ChatMessageResponse saveMessage(ChatMessageRequest request);

    List<ChatMessageResponse> getMessages(String conversationId);

    void clearMessages(String conversationId);
}
