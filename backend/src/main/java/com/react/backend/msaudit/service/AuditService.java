package com.react.backend.msaudit.service;

import com.react.backend.msaudit.dto.AuditEventResponse;
import com.react.backend.msaudit.model.AuditEvent;
import com.react.backend.msaudit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditRepository;

    public void saveEvent(String eventType, String aggregateId, String actor, String payload) {
        AuditEvent event = AuditEvent.builder()
                .eventType(eventType)
                .aggregateId(aggregateId)
                .actor(actor)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
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
        return AuditEventResponse.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .aggregateId(entity.getAggregateId())
                .actor(entity.getActor())
                .payload(entity.getPayload())
                .timestamp(entity.getTimestamp())
                .build();
    }
}