package com.andesstay.mscatalog.messaging;

import com.andesstay.mscatalog.model.Unit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaPublisher {
    public static final String TOPIC_CATALOG = "catalog.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUnitEvent(String eventType, Unit unit, String actor) {
        Map<String, Object> event = Map.of(
                "eventType", eventType,
                "unitId", unit.getUnitId(),
                "name", unit.getName(),
                "type", unit.getType().name(),
                "city", unit.getCity(),
                "availability", unit.getAvailability(),
                "actor", actor == null ? "SYSTEM" : actor,
                "timestamp", System.currentTimeMillis());
        kafkaTemplate.send(TOPIC_CATALOG, String.valueOf(unit.getUnitId()), event);
    }
}
