package com.connectsphere.post.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public SearchIndexEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.events.exchange}") String exchange,
            @Value("${app.events.search-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(SearchIndexEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
