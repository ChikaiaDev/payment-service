package com.ncba.notification.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String NOTIFICATION_QUEUE = "notification.queue";
}