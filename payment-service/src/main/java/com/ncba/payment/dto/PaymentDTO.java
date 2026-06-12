package com.ncba.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {

    @Data
    public static class PaymentRequest {
        @NotBlank(message = "Sender account is required")
        private String senderAccount;

        @NotBlank(message = "Receiver account is required")
        private String receiverAccount;

        @NotNull(message = "Sender account is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater that zero")
        private BigDecimal amount;

        @NotBlank(message = "Sender account is required")
        private String currency;
    }

    @Data
    public static class PaymentResponse {
        private Long id;
        private String transactionRef;
        private String senderAccount;
        private String receiverAccount;
        private BigDecimal amount;
        private String currency;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        public static <T> ApiResponse<T> success(String message, T data) {
            ApiResponse<T> r = new ApiResponse<>();
            r.success = true;
            r.message = message;
            r.data = data;
            r.timestamp = LocalDateTime.now();
            return r;
        }

        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> r = new ApiResponse<>();
            r.success = false;
            r.message = message;
            r.timestamp = LocalDateTime.now();
            return r;
        }
    }

    @Data
    public static class PaymentEvent {
        private Long paymentId;
        private String transactionRef;
        private String senderAccount;
        private String receiverAccount;
        private BigDecimal amount;
        private String currency;
        private String status;
        private LocalDateTime timestamp;
    }
}
