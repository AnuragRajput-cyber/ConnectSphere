package com.connectsphere.media.repository;

import com.connectsphere.media.entity.Story;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, String> {

    List<Story> findByAuthorIdAndActiveTrueOrderByCreatedAtDesc(String authorId);

    List<Story> findByAuthorIdInAndActiveTrueOrderByCreatedAtDesc(List<String> authorIds);

    Optional<Story> findByStoryIdAndActiveTrue(String storyId);

    List<Story> findByExpiresAtBeforeAndActiveTrue(Instant cutoff);
}
