package com.connectsphere.search.repository;

import com.connectsphere.search.entity.Hashtag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, String> {

    Optional<Hashtag> findByTag(String tag);

    List<Hashtag> findTop10ByOrderByPostCountDescLastUsedAtDesc();

    List<Hashtag> findByTagContainingIgnoreCaseOrderByPostCountDesc(String query);
}
