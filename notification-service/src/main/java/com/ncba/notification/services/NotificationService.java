package com.ncba.notification.services;

import com.ncba.notification.dto.PaymentEvent;
import com.ncba.notification.models.Notification;
import com.ncba.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void processEvent(PaymentEvent paymentEvent) {
        log.info("Received Payment Event ref: {} , Event status : is {} ", paymentEvent.getPaymentId(), paymentEvent.getStatus());

        Notification notification = new Notification();
        notification.setTransactionRef(paymentEvent.getTransactionRef());
        notification.setSenderAccount(paymentEvent.getSenderAccount());
        notification.setReceiverAccount(paymentEvent.getReceiverAccount());
        notification.setAmount(paymentEvent.getAmount());
        notification.setCurrency(paymentEvent.getCurrency());
        notification.setStatus(paymentEvent.getStatus());

        notificationRepository.save(notification);

        log.info("Notification processed successfully. Ref {} ", paymentEvent.getTransactionRef());

    }
}
