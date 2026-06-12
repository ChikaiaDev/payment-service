package com.ncba.reporting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentEvent {
    private Long paymentId;
    private String transactionRef;
    private String senderAccount;
    private String receiverAccount;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime timestamp;
}
