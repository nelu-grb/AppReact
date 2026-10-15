package com.react.backend.msaudit.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record AuditEventResponse(
        Long id,
        Instant timestamp,
        String eventType,
        String action,
        String entityType,
        String entityId,
        String aggregateId,
        String reservationId,
        Long unitId,
        String unitName,
        String actor,
        String performedBy,
        JsonNode previousState,
        JsonNode newState,
        String details,
        JsonNode payload
) {}
