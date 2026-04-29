package com.connectsphere.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {

    @Bean
    MessageConverter notificationMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange notificationEventsExchange(@Value("${app.events.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue notificationQueue(@Value("${app.events.notification-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding notificationBinding(
            Queue notificationQueue,
            TopicExchange notificationEventsExchange,
            @Value("${app.events.notification-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(notificationQueue).to(notificationEventsExchange).with(routingKey);
    }
}
