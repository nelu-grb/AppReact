package com.andesstay.reservations.service;

import com.andesstay.reservations.config.KafkaConfig;
import com.andesstay.reservations.config.RabbitMQConfig;
import com.andesstay.reservations.dto.CreateReservationDTO;
import com.andesstay.reservations.dto.EnvelopeDTO;
import com.andesstay.reservations.model.Reservation;
import com.andesstay.reservations.model.ReservationStatus;
import com.andesstay.reservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Reservation createReservation(CreateReservationDTO dto) {
        Reservation reservation = Reservation.builder()
                .guestId(dto.getGuestId())
                .unitId(dto.getUnitId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalAmount(dto.getTotalAmount())
                .status(ReservationStatus.CREADA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reserva creada exitosamente con ID: {}", saved.getId());

        publishKafkaEvent("RESERVATION_CREATED", saved);
        return saved;
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
    }

    public List<Reservation> searchReservations(ReservationStatus status, LocalDate from, LocalDate to) {
        return reservationRepository.findByFilters(status, from, to);
    }

    @Transactional
    public Reservation updateStatus(Long id, ReservationStatus newStatus, String actor) {
        Reservation reservation = getById(id);
        ReservationStatus currentStatus = reservation.getStatus();

        // Regla clave: No se puede hacer check-in sin estar CONFIRMADA
        if ((newStatus == ReservationStatus.CHECKIN_PENDIENTE || newStatus == ReservationStatus.EN_ESTADÍA)
                && currentStatus != ReservationStatus.CONFIRMADA) {
            throw new IllegalStateException(
                "No se puede realizar check-in si la reserva no está CONFIRMADA. Estado actual: " + currentStatus
            );
        }

        reservation.setStatus(newStatus);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation updated = reservationRepository.save(reservation);

        log.info("Reserva ID: {} actualizó su estado a {} por el usuario {}", id, newStatus, actor);

        handleStatusTransitions(updated, actor);
        return updated;
    }

    private void handleStatusTransitions(Reservation reservation, String actor) {
        // Enviar evento de streaming a Kafka
        publishKafkaEvent("RESERVATION_STATUS_CHANGED", reservation);

        // Disparar comandos a colas de RabbitMQ
        switch (reservation.getStatus()) {
            case CONFIRMADA:
                sendRabbitMessage(RabbitMQConfig.EXCHANGE_DIRECT, "email.send", buildEnvelope("EMAIL_CONFIRMATION", reservation));
                sendRabbitMessage(RabbitMQConfig.EXCHANGE_DIRECT, "voucher.gen", buildEnvelope("GENERATE_VOUCHER", reservation));
                break;
            case CHECKIN_PENDIENTE:
                sendRabbitMessage(RabbitMQConfig.EXCHANGE_DIRECT, "housekeeping.ticket", buildEnvelope("HOUSEKEEPING_TICKET", reservation));
                break;
            case CHECKOUT:
                sendRabbitMessage(RabbitMQConfig.EXCHANGE_DIRECT, "email.send", buildEnvelope("EMAIL_CHECKOUT", reservation));
                break;
            default:
                break;
        }
    }

    private <T> EnvelopeDTO<T> buildEnvelope(String eventType, T payload) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        return EnvelopeDTO.<T>builder()
                .type(eventType)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .traceId(traceId)
                .correlationId(UUID.randomUUID().toString())
                .payload(payload)
                .build();
    }

    private void sendRabbitMessage(String exchange, String routingKey, EnvelopeDTO<?> envelope) {
        rabbitTemplate.convertAndSend(exchange, routingKey, envelope);
    }

    private void publishKafkaEvent(String eventType, Reservation reservation) {
        EnvelopeDTO<Reservation> envelope = buildEnvelope(eventType, reservation);
        kafkaTemplate.send(KafkaConfig.TOPIC_RESERVATIONS_EVENTS, reservation.getId().toString(), envelope);
    }
}