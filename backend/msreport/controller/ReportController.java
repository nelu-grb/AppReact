package com.react.backend.msreport.controller;

import com.react.backend.msreport.model.Report;
import com.react.backend.msreport.repository.ReportRepository;
import com.react.backend.msreport.service.KpiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;
    private final KpiService kpiService;

    public ReportController(ReportRepository reportRepository, KpiService kpiService) {
        this.reportRepository = reportRepository;
        this.kpiService = kpiService;
    }

    @GetMapping
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @GetMapping("/kpis")
    public Map<String, Object> getKpis() {
        return kpiService.getKpis();
    }

    @GetMapping("/kpis/reservations-by-hour")
    public Map<String, Long> getReservationsByHour() {
        return kpiService.reservationsByHour();
    }

    @GetMapping("/kpis/active-occupancy")
    public long getActiveOccupancy() {
        return kpiService.activeOccupancy();
    }

    @GetMapping("/kpis/cycle-time")
    public double getAverageCycleTimeMinutes() {
        return kpiService.averageCycleTimeMinutes();
    }
}