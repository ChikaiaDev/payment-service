package com.ncba.notification.services;


import com.ncba.notification.config.RabbitMQConfig;
import com.ncba.notification.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    public PaymentEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Notification received for ref={} amount={} {}",
                event.getTransactionRef(),
                event.getAmount(),
                event.getCurrency());

        log.info("received notification payment event");
        notificationService.processEvent(event);
    }
}
