package com.andesstay.msreservations.service;

import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.messaging.KafkaPublisher;
import com.andesstay.msreservations.messaging.RabbitMQPublisher;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceRabbitMQTest {

    private ReservationRepository reservationRepository;
    private RabbitMQPublisher rabbitPublisher;
    private KafkaPublisher kafkaPublisher;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        rabbitPublisher = mock(RabbitMQPublisher.class);
        kafkaPublisher = mock(KafkaPublisher.class);
        UnitValidationService unitValidationService = mock(UnitValidationService.class);

        reservationService = new ReservationService(
                reservationRepository,
                rabbitPublisher,
                kafkaPublisher,
                unitValidationService);
        ReflectionTestUtils.setField(reservationService, "emailNotificationsEnabled", false);
    }

    @Test
    void invalidStatusTransitionDoesNotPublishRabbitMessage() {
        Reservation reservation = reservationInStatus(ReservationStatus.CREADA);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        StatusUpdateRequest request = statusRequest(ReservationStatus.EN_ESTADIA);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.updateStatus(100L, request, "operator"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(rabbitPublisher, never()).publishEmailCommand(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        verify(rabbitPublisher, never()).publishHousekeepingTicket(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        verify(rabbitPublisher, never()).publishVoucherGenCommand(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                any(BigDecimal.class),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void confirmationPublishesItsRabbitCommandsAndKafkaEvent() {
        Reservation reservation = reservationInStatus(ReservationStatus.CREADA);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.updateStatus(100L, statusRequest(ReservationStatus.CONFIRMADA), "operator");

        verify(rabbitPublisher).publishHousekeepingTicket(
                42L, "Preparar habitación para reserva #100", "RES-100");
        verify(rabbitPublisher).publishVoucherGenCommand(
                100L, "guest@example.com", BigDecimal.valueOf(250), "RES-100");
        verify(kafkaPublisher).publishReservationEvent("RESERVATION_STATUS_UPDATED", reservation, "operator");
    }

    private Reservation reservationInStatus(ReservationStatus status) {
        return Reservation.builder()
                .id(100L)
                .unitId(42L)
                .guestId("guest")
                .guestEmail("guest@example.com")
                .ownerOid("owner")
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 11, 3))
                .totalAmount(BigDecimal.valueOf(250))
                .status(status)
                .build();
    }

    private StatusUpdateRequest statusRequest(ReservationStatus status) {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(status);
        return request;
    }
}
