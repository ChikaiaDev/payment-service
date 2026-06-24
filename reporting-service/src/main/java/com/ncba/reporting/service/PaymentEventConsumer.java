package com.ncba.reporting.service;

import com.ncba.reporting.config.RabbitMQConfig;
import com.ncba.reporting.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventConsumer {
    private final ReportingService reportingService;

    public PaymentEventConsumer(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @RabbitListener(queues = RabbitMQConfig.REPORTING_QUEUE)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Reporting received for ref={} status={}",
                event.getTransactionRef(),
                event.getStatus());

        log.info("received payment report event");
        reportingService.reportEvent(event);
    }
}