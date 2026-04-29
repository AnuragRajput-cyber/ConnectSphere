package com.connectsphere.comment.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public NotificationEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.events.exchange}") String exchange,
            @Value("${app.events.notification-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(SocialNotificationEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
