package com.connectsphere.search.repository;

import com.connectsphere.search.document.HashtagSearchDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HashtagSearchRepository extends ElasticsearchRepository<HashtagSearchDocument, String> {

    List<HashtagSearchDocument> findTop10ByOrderByPostCountDescLastUsedAtDesc();

    List<HashtagSearchDocument> findTop20ByTagContainingIgnoreCaseOrderByPostCountDesc(String tag);
}
