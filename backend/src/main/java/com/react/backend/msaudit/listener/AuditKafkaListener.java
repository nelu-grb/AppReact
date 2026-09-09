package com.react.backend.msaudit.listener;

import com.react.backend.msaudit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaListener {

    private final AuditService auditService;

    @KafkaListener(topics = {"reservation-events", "catalog-events"}, groupId = "audit-group")
    public void consume(String message) {
        log.info("Evento de auditoría capturado desde Kafka: {}", message);
        auditService.saveEvent("DOMAIN_EVENT", "N/A", "KAFKA_CONSUMER", message);
    }
}