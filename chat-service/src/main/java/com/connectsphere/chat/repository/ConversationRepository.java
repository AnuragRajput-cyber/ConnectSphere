package com.connectsphere.chat.repository;

import com.connectsphere.chat.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("""
            select c
            from Conversation c
            where (c.participantOneId = :first and c.participantTwoId = :second)
               or (c.participantOneId = :second and c.participantTwoId = :first)
            """)
    Optional<Conversation> findBetween(@Param("first") String first, @Param("second") String second);

    List<Conversation> findByParticipantOneIdOrParticipantTwoId(String participantOneId, String participantTwoId);
}
