package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.messaging.KafkaPublisher;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.repository.ReservationRepository;
import com.andesstay.msreservations.service.ReservationService;
import com.andesstay.msreservations.service.WebpayService;
import com.andesstay.msreservations.service.WebpayService.WebpayCommitResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    @Test
    void successfulTransbankCommitRecordsPaymentAndValidStatusTransitions() {
        WebpayService webpayService = mock(WebpayService.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        KafkaPublisher kafkaPublisher = mock(KafkaPublisher.class);
        PaymentController controller = new PaymentController(
                webpayService,
                reservationRepository,
                reservationService,
                kafkaPublisher
        );
        Reservation reservation = new Reservation();
        reservation.setId(42L);
        reservation.setUnitId(3L);
        reservation.setGuestId("guest-42");
        reservation.setStatus(ReservationStatus.CREADA);
        WebpayCommitResponse payment = new WebpayCommitResponse();
        payment.setResponseCode(0);
        payment.setStatus("AUTHORIZED");
        payment.setBuyOrder("ORD-42");
        payment.setAmount(new BigDecimal("135000"));
        when(webpayService.commitTransaction("webpay-token")).thenReturn(payment);
        when(reservationRepository.findById(42L)).thenReturn(Optional.of(reservation));

        var response = controller.confirmarPago("webpay-token", null, null, null);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(
                "http://localhost:5173/payment-result?status=success&id=42",
                response.getHeaders().getFirst("Location")
        );
        var order = inOrder(kafkaPublisher, reservationService);
        order.verify(kafkaPublisher).publishPaymentAuthorized(same(reservation), same(payment));
        order.verify(reservationService).updateStatus(
                eq(42L),
                org.mockito.ArgumentMatchers.argThat(
                        (com.andesstay.msreservations.dto.StatusUpdateRequest request) ->
                                request.getStatus() == ReservationStatus.CONFIRMADA
                ),
                eq("TRANSBANK"),
                eq("Transbank")
        );
        order.verify(reservationService).updateStatus(
                eq(42L),
                org.mockito.ArgumentMatchers.argThat(
                        (com.andesstay.msreservations.dto.StatusUpdateRequest request) ->
                                request.getStatus() == ReservationStatus.CHECKIN_PENDIENTE
                ),
                eq("TRANSBANK"),
                eq("Transbank")
        );
    }
}
