package com.react.backend.msreport.listener;

import com.react.backend.msreport.model.Report;
import com.react.backend.msreport.repository.ReportRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportEventListener {

    private final ReportRepository reportRepository;

    public ReportEventListener(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @KafkaListener(topics = "reservation-events", groupId = "report-group")
    public void consumeReservationEvent(String message) {
        Report report = new Report("RESERVATION_EVENT", message, LocalDateTime.now());
        reportRepository.save(report);
        System.out.println("[ms-report] Reporte generado a partir de evento: " + message);
    }
}