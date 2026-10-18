package com.react.backend.msaudit.listener;

import com.react.backend.msaudit.service.AuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditKafkaListener.class);

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditKafkaListener(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "audit.timeline", groupId = "audit-group")
    public void consume(String message) {
        log.info("Evento de auditoría capturado desde Kafka: {}", message);
        try {
            JsonNode event = objectMapper.readTree(message);
            auditService.saveEvent(
                    event.path("eventType").asText("DOMAIN_EVENT"),
                        event.has("reservationId")
                            ? event.path("reservationId").asText()
                            : event.path("unitId").asText("N/A"),
                    event.path("actor").asText("KAFKA_CONSUMER"),
                    message);
        } catch (Exception exception) {
            log.warn("Evento Kafka no tiene el formato esperado", exception);
        }
    }
}