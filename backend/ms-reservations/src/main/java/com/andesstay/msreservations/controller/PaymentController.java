package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.PaymentInitResponseDto;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.messaging.KafkaPublisher;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.repository.ReservationRepository;
import com.andesstay.msreservations.service.ReservationService;
import com.andesstay.msreservations.service.WebpayService;
import com.andesstay.msreservations.service.WebpayService.WebpayCommitResponse;
import com.andesstay.msreservations.service.WebpayService.WebpayCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class PaymentController {

    private final WebpayService webpayService;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final KafkaPublisher kafkaPublisher;

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> iniciarPago(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

        try {
            String sessionId = "SESS-" + reservation.getGuestId();
            WebpayCreateResponse response = webpayService.createTransaction(
                    reservation.getId(),
                    sessionId,
                    reservation.getTotalAmount()
            );

            return ResponseEntity.ok(new PaymentInitResponseDto(response.getUrl(), response.getToken()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al iniciar pago en Webpay: " + e.getMessage()));
        }
    }

// Importa RequestMethod si no lo tienes: import org.springframework.web.bind.annotation.RequestMethod;

    @RequestMapping(value = "/confirm-payment", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmarPago(
            @RequestParam(value = "token_ws", required = false) String tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) String tbkToken,
            @RequestParam(value = "TBK_ORDEN_COMPRA", required = false) String tbkOrdenCompra,
            @RequestParam(value = "TBK_ID_SESION", required = false) String tbkIdSesion) {

        try {
            // 1. Si el usuario canceló el pago en Transbank
            if (tokenWs == null && tbkToken != null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "http://localhost:5173/payment-result?status=cancelled")
                        .build();
            }

            // 2. Si Transbank no envió ningún token válido
            if (tokenWs == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "http://localhost:5173/payment-result?status=error")
                        .build();
            }

            // 3. Confirmar la transacción con Transbank
            WebpayCommitResponse response = webpayService.commitTransaction(tokenWs);

            if (response.getResponseCode() == 0 && "AUTHORIZED".equals(response.getStatus())) {
                Long reservationId = Long.parseLong(response.getBuyOrder().replace("ORD-", ""));
                Reservation reservation = reservationRepository.findById(reservationId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Reserva no encontrada con ID: " + reservationId
                        ));

                kafkaPublisher.publishPaymentAuthorized(reservation, response);

                if (reservation.getStatus() == ReservationStatus.CREADA) {
                    updateReservationStatus(reservationId, ReservationStatus.CONFIRMADA);
                }
                if (reservation.getStatus() == ReservationStatus.CREADA
                        || reservation.getStatus() == ReservationStatus.CONFIRMADA) {
                    updateReservationStatus(reservationId, ReservationStatus.CHECKIN_PENDIENTE);
                }

                // Rebotar al frontend con éxito
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "http://localhost:5173/payment-result?status=success&id=" + reservationId)
                        .build();
            } else {
                // Si el banco rechazó la tarjeta
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "http://localhost:5173/payment-result?status=rejected")
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/payment-result?status=error")
                    .build();
        }
    }

    private void updateReservationStatus(Long reservationId, ReservationStatus status) {
        StatusUpdateRequest statusRequest = new StatusUpdateRequest();
        statusRequest.setStatus(status);
        reservationService.updateStatus(reservationId, statusRequest, "TRANSBANK", "Transbank");
    }
}