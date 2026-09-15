package com.andesstay.mscatalog.messaging;

import com.andesstay.mscatalog.model.Unit;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Service
public class KafkaPublisher {
    public static final String TOPIC_CATALOG = "catalog.events";
    public static final String TOPIC_AUDIT = "audit.timeline";
    private static final Logger logger = LoggerFactory.getLogger(KafkaPublisher.class);

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
        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC_CATALOG, String.valueOf(unit.getUnitId()), event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            logger.warn("Could not publish catalog event {} for unit {}", eventType, unit.getUnitId(), error);
                        }
                    });
                kafkaTemplate.send(TOPIC_AUDIT, String.valueOf(unit.getUnitId()), event);
            } catch (RuntimeException error) {
                logger.warn("Could not publish catalog event {} for unit {}", eventType, unit.getUnitId(), error);
            }
        });
    }
}
