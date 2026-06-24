package com.ncba.payment.queue;

import com.ncba.payment.config.RabbitMQConfig;
import com.ncba.payment.dto.PaymentDTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventPublisher {
    private final AmqpTemplate amqpTemplate;

    public PaymentEventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publishNotificationEvent(PaymentEvent event) {
        log.info("Publishing notification event ref={}", event.getTransactionRef());
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                event
        );
    }

    public void publishReportingEvent(PaymentEvent event) {
        log.info("Publishing reporting event ref={}", event.getTransactionRef());
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.REPORTING_ROUTING_KEY,
                event
        );
    }
}
