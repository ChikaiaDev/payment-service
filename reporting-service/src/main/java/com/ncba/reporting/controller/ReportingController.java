package com.ncba.reporting.controller;



import com.ncba.reporting.dto.PaymentEvent;
import com.ncba.reporting.model.PaymentReport;
import com.ncba.reporting.service.ReportingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reports")
@Slf4j
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveReport(@RequestBody PaymentEvent paymentEvent) {
        log.info("received payment report event");
        reportingService.reportEvent(paymentEvent);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PaymentReport>> getAllReports() {
        log.info("received get all reports request");
        return ResponseEntity.ok().body(reportingService.getAllReports());
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentReport>> getAllReportsByStatus(@PathVariable String status) {
        log.info("received get all {} reports request", status);
        return ResponseEntity.ok().body(reportingService.getAllReportsByStatus(status));
    }
}
