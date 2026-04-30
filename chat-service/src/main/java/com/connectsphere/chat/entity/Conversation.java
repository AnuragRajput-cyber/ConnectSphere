package com.connectsphere.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @Column(name = "conversation_id", nullable = false, updatable = false, length = 36)
    private String conversationId;

    @Column(name = "participant_one_id", nullable = false, length = 36)
    private String participantOneId;

    @Column(name = "participant_two_id", nullable = false, length = 36)
    private String participantTwoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString();
        }
        createdAt = Instant.now();
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getParticipantOneId() {
        return participantOneId;
    }

    public void setParticipantOneId(String participantOneId) {
        this.participantOneId = participantOneId;
    }

    public String getParticipantTwoId() {
        return participantTwoId;
    }

    public void setParticipantTwoId(String participantTwoId) {
        this.participantTwoId = participantTwoId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
