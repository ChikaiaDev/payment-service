package com.ncba.notification.controller;


import com.ncba.notification.dto.PaymentEvent;
import com.ncba.notification.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/notification")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveNotification(@RequestBody PaymentEvent paymentEvent) {
        log.info("received notification payment event");
        notificationService.processEvent(paymentEvent);

        return ResponseEntity.ok().build();
    }
}
