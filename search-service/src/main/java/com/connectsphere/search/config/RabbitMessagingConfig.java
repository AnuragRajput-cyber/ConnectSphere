package com.connectsphere.search.config;

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
    MessageConverter searchMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange searchEventsExchange(@Value("${app.events.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue searchQueue(@Value("${app.events.search-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding searchBinding(
            Queue searchQueue,
            TopicExchange searchEventsExchange,
            @Value("${app.events.search-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(searchQueue).to(searchEventsExchange).with(routingKey);
    }
}
