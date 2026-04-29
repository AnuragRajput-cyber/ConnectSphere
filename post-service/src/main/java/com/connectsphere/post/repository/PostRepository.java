package com.connectsphere.post.repository;

import com.connectsphere.post.entity.Post;
import com.connectsphere.post.entity.PostVisibility;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, String> {

    List<Post> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(String authorId);

    Optional<Post> findByPostIdAndDeletedFalse(String postId);

    List<Post> findByVisibilityAndDeletedFalseOrderByCreatedAtDesc(PostVisibility visibility);
    List<Post> findByDeletedFalseOrderByCreatedAtDesc();

    @Query("""
            select p
            from Post p
            where p.deleted = false
              and p.authorId in :userIds
            order by p.createdAt desc
            """)
    List<Post> findFeedByUserIds(@Param("userIds") Collection<String> userIds);

    @Query("""
            select p
            from Post p
            where p.deleted = false
              and lower(p.content) like lower(concat('%', :query, '%'))
            order by p.createdAt desc
            """)
    List<Post> searchByContent(@Param("query") String query);

    List<Post> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    long countByAuthorIdAndDeletedFalse(String authorId);
}
