package com.connectsphere.like.repository;

import com.connectsphere.like.entity.Like;
import com.connectsphere.like.entity.TargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, String> {

    Optional<Like> findByUserIdAndTargetIdAndTargetType(String userId, String targetId, TargetType targetType);

    List<Like> findByTargetId(String targetId);

    List<Like> findByUserId(String userId);

    List<Like> findByTargetIdAndTargetType(String targetId, TargetType targetType);

    boolean existsByUserIdAndTargetIdAndTargetType(String userId, String targetId, TargetType targetType);

    long countByTargetId(String targetId);

    long countByTargetIdAndTargetType(String targetId, TargetType targetType);

    void deleteByUserIdAndTargetIdAndTargetType(String userId, String targetId, TargetType targetType);

    @Query("""
            select l.reactionType as reaction, count(l) as total
            from Like l
            where l.targetId = :targetId and l.targetType = :targetType
            group by l.reactionType
            """)
    List<Object[]> getReactionSummary(@Param("targetId") String targetId, @Param("targetType") TargetType targetType);
}
