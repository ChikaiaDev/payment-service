package com.ncba.reporting.repository;

import com.ncba.reporting.model.PaymentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportingRepository extends JpaRepository<PaymentReport, Long> {
    Optional<List<PaymentReport>> findByStatus(String status);
}
