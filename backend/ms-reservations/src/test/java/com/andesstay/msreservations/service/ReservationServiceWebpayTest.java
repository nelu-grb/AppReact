package com.andesstay.msreservations.service;

import com.andesstay.msreservations.messaging.KafkaPublisher;
import com.andesstay.msreservations.messaging.RabbitMQPublisher;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceWebpayTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RabbitMQPublisher rabbitPublisher;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
    private UnitValidationService unitValidationService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void authorizedPaymentMovesConfirmedReservationToPaidStatus() {
        Reservation reservation = reservation(ReservationStatus.CONFIRMADA);
        when(reservationRepository.findById(42L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.confirmWebpayPayment(42L, new BigDecimal("12000"));

        assertEquals(ReservationStatus.CHECKIN_PENDIENTE, reservation.getStatus());
        verify(kafkaPublisher).publishReservationEvent(
                "RESERVATION_STATUS_UPDATED", reservation, "SYSTEM_WEBPAY");
    }

    @Test
    void repeatedAuthorizedCallbackDoesNotChangePaidReservationAgain() {
        Reservation reservation = reservation(ReservationStatus.CHECKIN_PENDIENTE);
        when(reservationRepository.findById(42L)).thenReturn(Optional.of(reservation));

        reservationService.confirmWebpayPayment(42L, new BigDecimal("12000"));

        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(kafkaPublisher, never()).publishReservationEvent(any(), any(), any());
    }

    @Test
    void rejectsPaymentWithDifferentAmount() {
        Reservation reservation = reservation(ReservationStatus.CONFIRMADA);
        when(reservationRepository.findById(42L)).thenReturn(Optional.of(reservation));

        assertThrows(ResponseStatusException.class,
                () -> reservationService.confirmWebpayPayment(42L, new BigDecimal("11999")));

        assertEquals(ReservationStatus.CONFIRMADA, reservation.getStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    private Reservation reservation(ReservationStatus status) {
        return Reservation.builder()
                .id(42L)
                .totalAmount(new BigDecimal("12000"))
                .status(status)
                .build();
    }
}
