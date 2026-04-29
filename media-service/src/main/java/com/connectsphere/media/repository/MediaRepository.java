package com.connectsphere.media.repository;

import com.connectsphere.media.entity.Media;
import com.connectsphere.media.entity.MediaType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, String> {

    List<Media> findByUploaderIdAndDeletedFalse(String uploaderId);

    Optional<Media> findByMediaIdAndDeletedFalse(String mediaId);

    List<Media> findByLinkedPostIdAndDeletedFalse(String linkedPostId);

    List<Media> findByMediaTypeAndDeletedFalse(MediaType mediaType);

    void deleteByMediaId(String mediaId);
}
