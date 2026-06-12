package com.ncba.notification.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@NoArgsConstructor
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionRef;
    private String senderAccount;
    private String receiverAccount;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String message;
    private LocalDateTime receivedAt;
    @PrePersist protected void onCreate() { receivedAt = LocalDateTime.now(); }

}
