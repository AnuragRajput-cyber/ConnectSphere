package com.connectsphere.chat.service;

import com.connectsphere.chat.dto.ChatMessageRequest;
import com.connectsphere.chat.dto.ChatMessageResponse;
import com.connectsphere.chat.dto.ConversationRequest;
import com.connectsphere.chat.dto.ConversationResponse;
import com.connectsphere.chat.entity.ChatMessage;
import com.connectsphere.chat.entity.Conversation;
import com.connectsphere.chat.repository.ChatMessageRepository;
import com.connectsphere.chat.repository.ConversationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatServiceImpl(ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ConversationResponse createOrGetConversation(ConversationRequest request) {
        Conversation conversation = conversationRepository.findBetween(
                        request.participantOneId().trim(),
                        request.participantTwoId().trim())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setParticipantOneId(request.participantOneId().trim());
                    created.setParticipantTwoId(request.participantTwoId().trim());
                    return conversationRepository.save(created);
                });
        return ConversationResponse.from(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(String userId) {
        return conversationRepository.findByParticipantOneIdOrParticipantTwoId(userId.trim(), userId.trim()).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @Override
    public ChatMessageResponse saveMessage(ChatMessageRequest request) {
        ConversationResponse conversation = createOrGetConversation(new ConversationRequest(request.senderId(), request.recipientId()));
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? conversation.conversationId()
                : request.conversationId().trim();
        if (!conversationId.equals(conversation.conversationId())) {
            throw new IllegalArgumentException("Conversation does not match sender and recipient.");
        }
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(request.senderId().trim());
        message.setRecipientId(request.recipientId().trim());
        message.setContent(request.content().trim());
        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderBySentAtAsc(conversationId.trim()).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Override
    public void clearMessages(String conversationId) {
        chatMessageRepository.deleteByConversationId(conversationId.trim());
    }
}
