package com.connectsphere.follow.repository;

import com.connectsphere.follow.entity.Follow;
import com.connectsphere.follow.entity.FollowStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, String> {

    Optional<Follow> findByFollowerIdAndFolloweeId(String followerId, String followeeId);

    List<Follow> findByFollowerId(String followerId);

    List<Follow> findByFolloweeId(String followeeId);

    List<Follow> findByFollowerIdAndStatus(String followerId, FollowStatus status);

    List<Follow> findByFolloweeIdAndStatus(String followeeId, FollowStatus status);

    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId);

    boolean existsByFollowerIdAndFolloweeIdAndStatus(String followerId, String followeeId, FollowStatus status);

    long countByFollowerId(String followerId);

    long countByFolloweeId(String followeeId);

    long countByFollowerIdAndStatus(String followerId, FollowStatus status);

    long countByFolloweeIdAndStatus(String followeeId, FollowStatus status);

    @Query("""
            select f.followeeId
            from Follow f
            where f.followerId = :userId
              and f.status = com.connectsphere.follow.entity.FollowStatus.ACTIVE
              and f.followeeId in (
                  select secondHop.followerId
                  from Follow secondHop
                  where secondHop.followeeId = :userId
                    and secondHop.status = com.connectsphere.follow.entity.FollowStatus.ACTIVE
              )
            """)
    List<String> findMutualFollows(@Param("userId") String userId);

    void deleteByFollowerIdAndFolloweeId(String followerId, String followeeId);
}
