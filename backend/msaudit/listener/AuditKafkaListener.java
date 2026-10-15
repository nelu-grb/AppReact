package com.react.backend.msaudit.listener;

import com.react.backend.msaudit.service.AuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    public void consume(ConsumerRecord<String, String> record) {
        String message = record.value();
        JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Evento de auditoría no contiene JSON válido", exception);
        }

        String eventType = event.path("eventType").asText();
        String aggregateId = event.hasNonNull("reservationId")
                ? event.path("reservationId").asText()
                : event.hasNonNull("unitId")
                        ? event.path("unitId").asText()
                        : event.path("entityId").asText();
        if (eventType.isBlank() || aggregateId.isBlank()) {
            throw new IllegalArgumentException("Evento de auditoría debe incluir eventType y un identificador");
        }

        try {
            auditService.saveEvent(
                    record.topic() + ":" + record.partition() + ":" + record.offset(),
                    eventType,
                    aggregateId,
                    event.path("actorId").asText(event.path("actor").asText("KAFKA_CONSUMER")),
                    message);
        } catch (RuntimeException exception) {
            log.error("No se pudo guardar el evento de auditoría {}", eventType, exception);
            throw exception;
        }
    }
}