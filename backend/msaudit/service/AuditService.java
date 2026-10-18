package com.react.backend.msaudit.service;

import com.react.backend.msaudit.dto.AuditEventResponse;
import com.react.backend.msaudit.model.AuditEvent;
import com.react.backend.msaudit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditEventRepository auditRepository;

    public AuditService(AuditEventRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void saveEvent(String eventType, String aggregateId, String actor, String payload) {
        AuditEvent event = new AuditEvent(null, eventType, aggregateId, actor, payload, LocalDateTime.now());
        auditRepository.save(event);
    }

    public List<AuditEventResponse> getAllEvents() {
        return auditRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditEventResponse> getTimelineByAggregate(String aggregateId) {
        return auditRepository.findByAggregateIdOrderByTimestampDesc(aggregateId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditEventResponse mapToResponse(AuditEvent entity) {
        return new AuditEventResponse(entity.getId(), entity.getEventType(), entity.getAggregateId(),
            entity.getActor(), entity.getPayload(), entity.getTimestamp());
    }
}