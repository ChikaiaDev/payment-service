package com.ncba.payment.service;

import com.ncba.payment.dto.PaymentDTO.*;
import com.ncba.payment.exception.PaymentNotFoundException;
import com.ncba.payment.exception.PaymentProcessingException;
import com.ncba.payment.models.*;
import com.ncba.payment.queue.PaymentEventPublisher;
import com.ncba.payment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DownstreamNotifier downstreamNotifier;
    private final PaymentEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    public PaymentService(PaymentRepository paymentRepository, DownstreamNotifier downstreamNotifier, PaymentEventPublisher eventPublisher, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.downstreamNotifier = downstreamNotifier;
        this.eventPublisher = eventPublisher;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        log.info("Processing payment from {} to {} amount {} {}",
                paymentRequest.getSenderAccount(), paymentRequest.getReceiverAccount(),
                paymentRequest.getAmount(), paymentRequest.getCurrency());

        // Validate
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().signum() <= 0) {
            throw new PaymentProcessingException("Payment amount must be positive.");
        }

        Payment payment = new Payment();
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setSenderAccount(paymentRequest.getSenderAccount());
        payment.setReceiverAccount(paymentRequest.getReceiverAccount());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setStatus(Payment.PaymentStatus.PROCESSING);

        Payment saved = paymentRepository.save(payment);
        log.info("Payment saved with ref={} status={}", saved.getTransactionRef(), saved.getStatus());

        // Simulate processing — mark as completed
        saved.setStatus(Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(saved);
        log.info("Payment completed ref={}", saved.getTransactionRef());

        // Publish event asynchronously to downstream services
        notifyDownstreamServices(saved, correlationId);

        MDC.clear();
        return toResponse(saved);
    }

    @Async
    public void notifyDownstreamServices(Payment savedPayment, String correlationId) {
        MDC.put("correlationId", correlationId);
        PaymentEvent event = toEvent(savedPayment);
        eventPublisher.publishNotificationEvent(event);
        eventPublisher.publishReportingEvent(event);
        MDC.clear();
    }


    private PaymentResponse toResponse(Payment savedPayment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(savedPayment.getId());
        response.setTransactionRef(savedPayment.getTransactionRef());
        response.setSenderAccount(savedPayment.getSenderAccount());
        response.setReceiverAccount(savedPayment.getReceiverAccount());
        response.setAmount(savedPayment.getAmount());
        response.setCurrency(savedPayment.getCurrency());
        response.setStatus(savedPayment.getStatus().name());
        response.setCreatedAt(savedPayment.getCreatedAt());
        response.setUpdatedAt(savedPayment.getUpdatedAt());
        return response;
    }

    private PaymentEvent toEvent(Payment savedPayment) {
        PaymentEvent response = new PaymentEvent();
        response.setPaymentId(savedPayment.getId());
        response.setTransactionRef(savedPayment.getTransactionRef());
        response.setSenderAccount(savedPayment.getSenderAccount());
        response.setReceiverAccount(savedPayment.getReceiverAccount());
        response.setAmount(savedPayment.getAmount());
        response.setCurrency(savedPayment.getCurrency());
        response.setStatus(savedPayment.getStatus().name());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
        log.info("Payment deleted id={}",id);
    }

    public PaymentResponse updatePayment(Long id, @Valid PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        payment.setSenderAccount(request.getSenderAccount());
        payment.setReceiverAccount(request.getReceiverAccount());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        log.info("Payment updated id={}", id);
        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return toResponse(payment);
    }
}
