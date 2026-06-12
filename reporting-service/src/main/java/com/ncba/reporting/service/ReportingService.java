package com.ncba.reporting.service;

import com.ncba.reporting.dto.PaymentEvent;
import com.ncba.reporting.model.PaymentReport;
import com.ncba.reporting.repository.ReportingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportingService {

    private final ReportingRepository reportingRepository;

    public ReportingService(ReportingRepository reportingRepository) {
        this.reportingRepository = reportingRepository;
    }


    public void reportEvent(PaymentEvent paymentEvent) {
        log.info("Recording report for ref={} status={}", paymentEvent.getTransactionRef(), paymentEvent.getStatus());
        PaymentReport report = new PaymentReport();
        report.setPaymentId(paymentEvent.getPaymentId());
        report.setTransactionRef(paymentEvent.getTransactionRef());
        report.setSenderAccount(paymentEvent.getSenderAccount());
        report.setReceiverAccount(paymentEvent.getReceiverAccount());
        report.setAmount(paymentEvent.getAmount());
        report.setCurrency(paymentEvent.getCurrency());
        report.setStatus(paymentEvent.getStatus());
        report.setPaymentTimestamp(paymentEvent.getTimestamp());
        reportingRepository.save(report);
        log.info("Report stored for ref={}", paymentEvent.getTransactionRef());
    }

    public List<PaymentReport> getAllReports() {
        return  reportingRepository.findAll();
    }

    public List<PaymentReport> getAllReportsByStatus(String status) {
        return reportingRepository.findByStatus(status.toUpperCase()).orElse(new ArrayList<>());
    }
}
