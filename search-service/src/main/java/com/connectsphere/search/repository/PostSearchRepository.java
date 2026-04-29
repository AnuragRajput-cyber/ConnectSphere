package com.connectsphere.search.repository;

import com.connectsphere.search.document.PostSearchDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostSearchDocument, String> {

    List<PostSearchDocument> findTop50ByContentContainingIgnoreCaseOrderByUpdatedAtDesc(String query);

    List<PostSearchDocument> findTop50ByHashtagsContainingOrderByUpdatedAtDesc(String hashtag);

    List<PostSearchDocument> findTop50ByContentContainingIgnoreCaseOrHashtagsContainingOrderByUpdatedAtDesc(String query, String hashtag);
}
