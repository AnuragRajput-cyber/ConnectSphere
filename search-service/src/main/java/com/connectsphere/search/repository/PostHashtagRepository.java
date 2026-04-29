package com.connectsphere.search.repository;

import com.connectsphere.search.entity.PostHashtag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, String> {

    List<PostHashtag> findByPostId(String postId);

    List<PostHashtag> findByHashtagId(String hashtagId);

    long countByHashtagId(String hashtagId);

    void deleteByPostId(String postId);
}
