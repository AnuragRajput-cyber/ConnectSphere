package com.connectsphere.comment.repository;

import com.connectsphere.comment.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(String postId);

    List<Comment> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(String authorId);

    Optional<Comment> findByCommentIdAndDeletedFalse(String commentId);

    List<Comment> findByParentCommentIdAndDeletedFalseOrderByCreatedAtAsc(String parentCommentId);

    @Query("""
            select c
            from Comment c
            where c.postId = :postId
              and c.parentCommentId is null
              and c.deleted = false
            order by c.createdAt asc
            """)
    List<Comment> findTopLevelByPostId(@Param("postId") String postId);

    long countByPostIdAndDeletedFalse(String postId);

    void deleteByCommentId(String commentId);
}
