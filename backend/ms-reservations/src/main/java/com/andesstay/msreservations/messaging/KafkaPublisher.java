package com.andesstay.msreservations.messaging;

import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.service.WebpayService.WebpayCommitResponse;
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

    public void publishReservationEvent(String eventType, Reservation reservation, String actor,
                                        String actorName, String unitName, ReservationStatus previousStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("entityType", "RESERVATION");
        event.put("reservationId", reservation.getId());
        event.put("unitId", reservation.getUnitId());
        event.put("unitName", unitName);
        event.put("guestId", reservation.getGuestId());
        event.put("status", reservation.getStatus().name());
        event.put("previousState", previousStatus == null ? null : previousStatus.name());
        event.put("newState", reservation.getStatus().name());
        event.put("startDate", reservation.getStartDate().toString());
        event.put("endDate", reservation.getEndDate().toString());
        event.put("createdAt", reservation.getCreatedAt().toString());
        event.put("updatedAt", reservation.getUpdatedAt().toString());
        event.put("actor", actor != null ? actor : "SYSTEM");
        event.put("actorName", actorName != null && !actorName.isBlank() ? actorName : actor);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC_RESERVATIONS, String.valueOf(reservation.getId()), event);
        kafkaTemplate.send(TOPIC_AUDIT, String.valueOf(reservation.getId()), event);
    }

    public void publishPaymentAuthorized(Reservation reservation, WebpayCommitResponse payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAYMENT_AUTHORIZED");
        event.put("entityType", "PAYMENT");
        event.put("entityId", reservation.getId());
        event.put("reservationId", reservation.getId());
        event.put("unitId", reservation.getUnitId());
        event.put("guestId", reservation.getGuestId());
        event.put("paymentProvider", "TRANSBANK");
        event.put("buyOrder", payment.getBuyOrder());
        event.put("amount", payment.getAmount());
        event.put("actorId", "TRANSBANK");
        event.put("actorName", "Transbank");
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC_AUDIT, String.valueOf(reservation.getId()), event);
    }
}