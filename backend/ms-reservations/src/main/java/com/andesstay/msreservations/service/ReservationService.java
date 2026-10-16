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
import com.andesstay.msreservations.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ReservationResponse createReservation(
            ReservationRequest request, AuthenticatedUser user, Jwt jwt) {
        unitValidationService.validateUnitAvailability(
                request.getUnitId(), request.getStartDate(), request.getEndDate(), jwt);

        UnitDto unit = unitValidationService.getUnit(request.getUnitId(), jwt);
        if (unit == null || unit.getPricePerNight() == null || unit.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo obtener el precio por noche de la unidad");
        }

        long nights = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (nights <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        BigDecimal totalAmount = unit.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .unitId(request.getUnitId())
                .guestId(user.requireName())
                .guestEmail(user.requireEmail())
                .ownerOid(user.requireOid())
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
        kafkaPublisher.publishReservationEvent("RESERVATION_CREATED", saved, user.actor());

        return mapToResponse(saved);
    }

    public ReservationResponse getReservationByIdForUser(Long id, AuthenticatedUser user) {
        return mapToResponse(getReservationForUser(id, user));
    }

    public Reservation getReservationForUser(Long id, AuthenticatedUser user) {
        return user.canViewAllReservations()
                ? reservationRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"))
                : reservationRepository.findByIdAndOwnerOid(id, user.requireOid())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    private List<ReservationResponse> getReservations(ReservationStatus status, LocalDate from, LocalDate to) {
        return reservationRepository.findByFilters(status, from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private List<ReservationResponse> getReservationsForOwner(
            String ownerOid, ReservationStatus status, LocalDate from, LocalDate to) {
        return reservationRepository.findByOwnerOidAndFilters(ownerOid, status, from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getReservationsForUser(
            AuthenticatedUser user, ReservationStatus status, LocalDate from, LocalDate to) {
        return user.canViewAllReservations()
                ? getReservations(status, from, to)
                : getReservationsForOwner(user.requireOid(), status, from, to);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ReservationStatus nextStatus = request.getStatus();

        if (!reservation.getStatus().canTransitionTo(nextStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar la reserva de " + reservation.getStatus() + " a " + nextStatus);
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

            // Novedad: Publicación del evento Pub/Sub al exchange Fanout
            Map<String, Object> evtPayload = new HashMap<>();
            evtPayload.put("reservationId", updated.getId());
            evtPayload.put("unitId", updated.getUnitId());
            evtPayload.put("status", "CONFIRMADA");
            rabbitPublisher.publishReservationConfirmed(updated.getId(), evtPayload);
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