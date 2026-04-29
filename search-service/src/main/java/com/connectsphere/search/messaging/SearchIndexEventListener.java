package com.connectsphere.search.messaging;

import com.connectsphere.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexEventListener {

    private final SearchService searchService;

    public SearchIndexEventListener(SearchService searchService) {
        this.searchService = searchService;
    }

    @RabbitListener(queues = "${app.events.search-queue}")
    public void onIndexEvent(PostIndexEvent event) {
        if (event == null || event.postId() == null || event.postId().isBlank()) {
            return;
        }
        if ("DELETE".equalsIgnoreCase(event.operation())) {
            searchService.removePostIndex(event.postId());
            return;
        }
        searchService.indexPost(event.postId(), event.content() == null ? "" : event.content());
    }
}
