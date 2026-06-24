package com.ncba.reporting.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String REPORTING_QUEUE = "reporting.queue";
}