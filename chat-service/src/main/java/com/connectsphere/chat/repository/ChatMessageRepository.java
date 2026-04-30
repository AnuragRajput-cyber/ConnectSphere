package com.connectsphere.chat.repository;

import com.connectsphere.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByConversationIdOrderBySentAtAsc(String conversationId);

    void deleteByConversationId(String conversationId);
}
