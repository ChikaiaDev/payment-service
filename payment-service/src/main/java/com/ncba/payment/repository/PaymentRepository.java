package com.ncba.payment.repository;

import com.ncba.payment.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByTransactionRef(String transactionRef);
    boolean existsByTransactionRef(String transactionRef);
}
