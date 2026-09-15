package com.react.backend.msaudit.controller;

import com.react.backend.msaudit.dto.AuditEventResponse;
import com.react.backend.msaudit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditEventResponse>> getAllAudits() {
        return ResponseEntity.ok(auditService.getAllEvents());
    }

    @GetMapping("/timeline/{aggregateId}")
    public ResponseEntity<List<AuditEventResponse>> getTimeline(@PathVariable String aggregateId) {
        return ResponseEntity.ok(auditService.getTimelineByAggregate(aggregateId));
    }
}