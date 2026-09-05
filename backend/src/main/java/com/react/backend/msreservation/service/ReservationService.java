package com.react.backend.msreservation.service;

import com.react.backend.msreservation.dto.ReservationRequest;
import com.react.backend.msreservation.dto.ReservationResponse;
import com.react.backend.msreservation.dto.StatusUpdateRequest;
import com.react.backend.msreservation.messaging.KafkaPublisher;
import com.react.backend.msreservation.messaging.RabbitMQPublisher;
import com.react.backend.msreservation.model.Reservation;
import com.react.backend.msreservation.model.ReservationStatus;
import com.react.backend.msreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RabbitMQPublisher rabbitPublisher;
    private final KafkaPublisher kafkaPublisher;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String currentUser) {
        Reservation reservation = Reservation.builder()
                .unitId(request.getUnitId())
                .guestId(request.getGuestId())
                .guestEmail(request.getGuestEmail())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(request.getTotalAmount())
                .status(ReservationStatus.CREADA)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        String correlationId = "RES-" + saved.getId();
        rabbitPublisher.publishEmailCommand(
                saved.getGuestEmail(), 
                "Reserva Registrada", 
                "Tu reserva #" + saved.getId() + " ha sido registrada con éxito.", 
                correlationId
        );
        kafkaPublisher.publishReservationEvent("RESERVATION_CREATED", saved, currentUser);

        return mapToResponse(saved);
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        return mapToResponse(reservation);
    }

    public List<ReservationResponse> getReservations(ReservationStatus status, LocalDate from, LocalDate to) {
        return reservationRepository.findByFilters(status, from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse updateStatus(Long id, StatusUpdateRequest request, String currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

        ReservationStatus nextStatus = request.getStatus();

        // Regla de Negocio: No se puede hacer check-in sin CONFIRMAR previamente
        if ((nextStatus == ReservationStatus.CHECKIN_PENDIENTE || nextStatus == ReservationStatus.EN_ESTADIA)
                && reservation.getStatus() != ReservationStatus.CONFIRMADA
                && reservation.getStatus() != ReservationStatus.CHECKIN_PENDIENTE) {
            throw new IllegalStateException("Error de Flujo: No se puede transicionar a Check-In sin confirmar la reserva previa.");
        }

        reservation.setStatus(nextStatus);
        Reservation updated = reservationRepository.save(reservation);

        String correlationId = "RES-" + updated.getId();

        if (nextStatus == ReservationStatus.CONFIRMADA) {
            rabbitPublisher.publishEmailCommand(
                    updated.getGuestEmail(), 
                    "Reserva Confirmada", 
                    "Tu reserva #" + updated.getId() + " ha sido confirmada.", 
                    correlationId
            );
            rabbitPublisher.publishHousekeepingTicket(
                    updated.getUnitId(), 
                    "Preparar habitación para reserva #" + updated.getId(), 
                    correlationId
            );
            rabbitPublisher.publishVoucherGenCommand(updated.getId(), correlationId);
        }

        kafkaPublisher.publishReservationEvent("RESERVATION_STATUS_UPDATED", updated, currentUser);

        return mapToResponse(updated);
    }

    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .unitId(r.getUnitId())
                .guestId(r.getGuestId())
                .guestEmail(r.getGuestEmail())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .totalAmount(r.getTotalAmount())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}