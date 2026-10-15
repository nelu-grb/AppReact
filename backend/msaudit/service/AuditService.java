package com.react.backend.msaudit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.react.backend.msaudit.dto.AuditEventResponse;
import com.react.backend.msaudit.model.AuditEvent;
import com.react.backend.msaudit.repository.AuditEventRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class AuditService {

    private final AuditEventRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    public void saveEvent(String eventId, String eventType, String aggregateId, String actor, String payload) {
        if (auditRepository.existsByEventId(eventId)) {
            return;
        }
        AuditEvent event = new AuditEvent(
                null, eventId, eventType, aggregateId, actor, payload, LocalDateTime.now()
        );
        auditRepository.save(event);
    }

    public List<AuditEventResponse> getAllEvents() {
        return auditRepository.findAll().stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(AuditEventResponse::timestamp).reversed())
                .toList();
    }

    public List<AuditEventResponse> getTimelineByAggregate(String aggregateId) {
        return auditRepository.findByAggregateIdOrderByTimestampDesc(aggregateId).stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(AuditEventResponse::timestamp).reversed())
                .toList();
    }

    private AuditEventResponse mapToResponse(AuditEvent entity) {
        JsonNode payload = parsePayload(entity);
        String reservationId = text(payload, "reservationId");
        String unitIdValue = text(payload, "unitId");
        Long unitId = parseLong(unitIdValue);
        String entityType = text(payload, "entityType");
        if (entityType == null) {
            entityType = reservationId != null || entity.getEventType().startsWith("RESERVATION_")
                    ? "RESERVATION"
                    : "UNIT";
        }

        String entityId = ("RESERVATION".equals(entityType) || "PAYMENT".equals(entityType))
                && reservationId != null
                ? reservationId
                : unitIdValue != null ? unitIdValue : entity.getAggregateId();
        String actorName = text(payload, "actorName");
        if (actorName == null || actorName.isBlank()) {
            actorName = entity.getActor();
        }

        JsonNode previousState = optionalNode(payload, "previousState");
        JsonNode newState = optionalNode(payload, "newState");
        String unitName = text(payload, "unitName");
        String action = action(entity.getEventType(), newState, payload);
        String details = details(entity.getEventType(), previousState, newState, unitName, payload);

        return new AuditEventResponse(
                entity.getId(),
                eventTimestamp(payload, entity.getTimestamp()),
                entity.getEventType(),
                action,
                entityType,
                entityId,
                entity.getAggregateId(),
                reservationId,
                unitId,
                unitName,
                entity.getActor(),
                actorName,
                previousState,
                newState,
                details,
                payload
        );
    }

    private JsonNode parsePayload(AuditEvent entity) {
        try {
            return objectMapper.readTree(entity.getPayload());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Stored audit payload is not valid JSON for event " + entity.getId(),
                    exception
            );
        }
    }

    private Instant eventTimestamp(JsonNode payload, LocalDateTime storedTimestamp) {
        JsonNode timestamp = payload.path("timestamp");
        if (timestamp.isNumber()) {
            return Instant.ofEpochMilli(timestamp.asLong());
        }
        if (timestamp.isTextual()) {
            String value = timestamp.asText();
            try {
                return Instant.parse(value);
            } catch (RuntimeException ignored) {
                try {
                    return OffsetDateTime.parse(value).toInstant();
                } catch (RuntimeException ignoredOffset) {
                    return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
                }
            }
        }
        return storedTimestamp.atZone(ZoneId.systemDefault()).toInstant();
    }

    private String action(String eventType, @Nullable JsonNode newState, JsonNode payload) {
        return switch (eventType) {
            case "RESERVATION_CREATED" -> "Reserva creada";
            case "RESERVATION_STATUS_UPDATED" -> {
                String status = stateValue(newState, payload.path("status"));
                yield status == null
                        ? "Estado de reserva actualizado"
                        : "Reserva " + statusLabel(status).toLowerCase(Locale.ROOT);
            }
            case "PAYMENT_AUTHORIZED" -> "Pago exitoso (Transbank)";
            case "UNIT_CREATED" -> "Unidad creada";
            case "UNIT_UPDATED" -> "Unidad actualizada";
            case "UNIT_DELETED" -> "Unidad eliminada";
            default -> eventType.replace('_', ' ');
        };
    }

    private String details(String eventType, @Nullable JsonNode previousState, @Nullable JsonNode newState,
                           String unitName, JsonNode payload) {
        String summary;
        if (eventType.equals("RESERVATION_STATUS_UPDATED")) {
            String previous = stateValue(previousState, payload.path("previousState"));
            String next = stateValue(newState, payload.path("newState"));
            if (next == null) {
                next = text(payload, "status");
            }
            summary = previous == null
                    ? (next == null ? "Estado de reserva actualizado" : "Estado: " + statusLabel(next))
                    : statusLabel(previous) + " → " + statusLabel(next);
        } else if (eventType.equals("RESERVATION_CREATED")) {
            summary = "Reserva creada";
        } else if (eventType.equals("PAYMENT_AUTHORIZED")) {
            String amount = text(payload, "amount");
            summary = amount == null
                    ? "Pago autorizado por Transbank"
                    : "Pago autorizado por Transbank — $" + amount;
        } else if (eventType.equals("UNIT_UPDATED")) {
            summary = changedUnitFields(previousState, newState);
        } else if (eventType.equals("UNIT_DELETED")) {
            summary = "Unidad eliminada";
        } else if (eventType.equals("UNIT_CREATED")) {
            summary = "Unidad creada";
        } else {
            summary = eventType.replace('_', ' ');
        }

        String resolvedUnitName = unitName;
        if (resolvedUnitName == null || resolvedUnitName.isBlank()) {
            resolvedUnitName = text(newState, "name");
        }
        if (resolvedUnitName == null || resolvedUnitName.isBlank()) {
            resolvedUnitName = text(payload, "name");
        }
        return resolvedUnitName == null || resolvedUnitName.isBlank()
                ? summary
                : summary + " — " + resolvedUnitName;
    }

    private String changedUnitFields(@Nullable JsonNode previousState, @Nullable JsonNode newState) {
        if (previousState == null || newState == null) {
            return "Unidad actualizada";
        }
        Map<String, String> labels = Map.of(
                "name", "Nombre",
                "type", "Tipo",
                "city", "Ciudad",
                "availability", "Disponibilidad",
                "pricePerNight", "Precio por noche",
                "maxOccupancy", "Capacidad",
                "rooms", "Habitaciones",
                "bathrooms", "Baños"
        );
        StringJoiner changes = new StringJoiner("; ");
        labels.forEach((field, label) -> {
            JsonNode oldValue = previousState.path(field);
            JsonNode newValue = newState.path(field);
            if (!oldValue.equals(newValue)) {
                changes.add(label + ": " + displayValue(oldValue) + " → " + displayValue(newValue));
            }
        });
        return changes.length() == 0 ? "Unidad actualizada" : changes.toString();
    }

    private String stateValue(@Nullable JsonNode preferred, JsonNode fallback) {
        if (preferred != null && preferred.isTextual()) {
            return preferred.asText();
        }
        return fallback.isTextual() ? fallback.asText() : null;
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "CREADA" -> "Creada";
            case "CONFIRMADA" -> "Confirmada";
            case "CHECKIN_PENDIENTE" -> "Check-in pendiente";
            case "EN_ESTADIA" -> "En estadía";
            case "CHECKOUT" -> "Check-out";
            case "CANCELADA" -> "Cancelada";
            default -> status.replace('_', ' ');
        };
    }

    private String displayValue(JsonNode value) {
        if (value == null || value.isMissingNode() || value.isNull()) {
            return "—";
        }
        return value.isTextual() ? value.asText() : value.toString();
    }

    private String text(@Nullable JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) {
            return null;
        }
        JsonNode value = node.path(field);
        return value.isValueNode() ? value.asText() : value.toString();
    }

    private JsonNode optionalNode(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value;
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}