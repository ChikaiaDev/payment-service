package com.ncba.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "payment.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String REPORTING_QUEUE = "reporting.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "payment.notification";
    public static final String REPORTING_ROUTING_KEY = "payment.reporting";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue reportingQueue() {
        return QueueBuilder.durable(REPORTING_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(paymentExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding reportingBinding() {
        return BindingBuilder
                .bind(reportingQueue())
                .to(paymentExchange())
                .with(REPORTING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
