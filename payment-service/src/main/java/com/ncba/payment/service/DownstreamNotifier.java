package com.ncba.payment.service;

import com.ncba.payment.dto.PaymentDTO.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class DownstreamNotifier {

    private final RestTemplate restTemplate;

    public DownstreamNotifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notificationFallback")
    @Retry(name = "notificationService")
    public void notifyNotificationService(PaymentEvent event) {
        log.info("Notifying notification-service for ref={}", event.getTransactionRef());
        restTemplate.postForEntity(
                "http://notification-service:8081/api/notifications",
                event,
                Void.class
        );
    }

    @CircuitBreaker(name = "reportingService", fallbackMethod = "reportingFallback")
    @Retry(name = "reportingService")
    public void notifyReportingService(PaymentEvent event) {
        log.info("Notifying reporting-service for ref={}", event.getTransactionRef());
        restTemplate.postForEntity(
                "http://reporting-service:8082/api/reporting",
                event,
                Void.class
        );
    }

    public void notificationFallback(PaymentEvent event, Throwable t) {
        log.warn("Notification service unavailable for ref={}, reason={}",
                event.getTransactionRef(), t.getMessage());
    }

    public void reportingFallback(PaymentEvent event, Throwable t) {
        log.warn("Reporting service unavailable for ref={}, reason={}",
                event.getTransactionRef(), t.getMessage());
    }
}