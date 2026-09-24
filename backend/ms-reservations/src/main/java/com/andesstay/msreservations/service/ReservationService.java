package com.andesstay.msreservations.service;

import com.andesstay.msreservations.dto.ReservationRequest;
import com.andesstay.msreservations.dto.ReservationResponse;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.dto.UnitDto;
import com.andesstay.msreservations.messaging.KafkaPublisher;
import com.andesstay.msreservations.messaging.RabbitMQPublisher;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RabbitMQPublisher rabbitPublisher;
    private final KafkaPublisher kafkaPublisher;
    private final UnitValidationService unitValidationService;

        @Value("${notifications.email.enabled:false}")
        private boolean emailNotificationsEnabled;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String currentUser) {
        unitValidationService.validateUnitAvailability(request.getUnitId(), request.getStartDate(), request.getEndDate());

        UnitDto unit = unitValidationService.getUnit(request.getUnitId());
        if (unit == null || unit.getPricePerNight() == null || unit.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No se pudo obtener el precio por noche de la unidad");
        }

        long nights = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (nights <= 0) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada");
        }

        BigDecimal totalAmount = unit.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .unitId(request.getUnitId())
                .guestId(request.getGuestId())
                .guestEmail(request.getGuestEmail())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(totalAmount)
                .status(ReservationStatus.CREADA)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        String correlationId = "RES-" + saved.getId();
        if (emailNotificationsEnabled) {
            rabbitPublisher.publishEmailCommand(
                    saved.getGuestEmail(),
                    "Reserva Registrada",
                    "Tu reserva #" + saved.getId() + " ha sido registrada con éxito.",
                    correlationId
            );
        }
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

    public List<ReservationResponse> getReservationsByUnit(Long unitId) {
        return reservationRepository.findByUnitId(unitId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCancelledReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        if (reservation.getStatus() != ReservationStatus.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden eliminar reservas canceladas"
            );
        }

        reservationRepository.delete(reservation);
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
            if (emailNotificationsEnabled) {
                rabbitPublisher.publishEmailCommand(
                        updated.getGuestEmail(),
                        "Reserva Confirmada",
                        "Tu reserva #" + updated.getId() + " ha sido confirmada.",
                        correlationId
                );
            }
            rabbitPublisher.publishHousekeepingTicket(
                    updated.getUnitId(), 
                    "Preparar habitación para reserva #" + updated.getId(), 
                    correlationId
            );
            rabbitPublisher.publishVoucherGenCommand(
                    updated.getId(),
                    updated.getGuestEmail(),
                    updated.getTotalAmount(),
                    correlationId
            );
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