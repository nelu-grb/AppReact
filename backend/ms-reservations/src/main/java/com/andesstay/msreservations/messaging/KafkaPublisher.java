package com.andesstay.msreservations.messaging;

import com.andesstay.msreservations.model.Reservation;
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
        event.put("startDate", reservation.getStartDate().toString());
        event.put("endDate", reservation.getEndDate().toString());
        event.put("createdAt", reservation.getCreatedAt().toString());
        event.put("updatedAt", reservation.getUpdatedAt().toString());
        event.put("actor", actor != null ? actor : "SYSTEM");
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC_RESERVATIONS, String.valueOf(reservation.getId()), event);
        kafkaTemplate.send(TOPIC_AUDIT, String.valueOf(reservation.getId()), event);
    }
}