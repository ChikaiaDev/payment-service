package com.ncba.reporting.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reports")
@Data
@NoArgsConstructor
public class PaymentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long paymentId;
    private String transactionRef;
    private String senderAccount;
    private String receiverAccount;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime paymentTimestamp;
    private LocalDateTime reportedAt;
    @PrePersist protected void onCreate() { reportedAt = LocalDateTime.now(); }
}
