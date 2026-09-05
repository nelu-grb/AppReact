package com.react.backend.msreservation.messaging;

import com.react.backend.msreservation.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC_RESERVATIONS = "reservations.events";
    public static final String TOPIC_AUDIT = "audit.timeline";

    public void publishReservationEvent(String eventType, Reservation reservation, String actor) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("reservationId", reservation.getId());
        event.put("unitId", reservation.getUnitId());
        event.put("guestId", reservation.getGuestId());
        event.put("status", reservation.getStatus().name());
        event.put("actor", actor != null ? actor : "SYSTEM");
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC_RESERVATIONS, String.valueOf(reservation.getId()), event);
        kafkaTemplate.send(TOPIC_AUDIT, String.valueOf(reservation.getId()), event);
    }
}