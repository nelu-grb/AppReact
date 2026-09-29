package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.PaymentInitResponseDto;
import com.andesstay.msreservations.entity.Reservation;
import com.andesstay.msreservations.repository.ReservationRepository;
import com.andesstay.msreservations.service.ReservationService;
import com.andesstay.msreservations.service.WebpayService;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
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

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> iniciarPago(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

        try {
            String sessionId = "SESS-" + reservation.getGuestId();
            WebpayPlusTransactionCreateResponse response = webpayService.createTransaction(
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

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmarPago(@RequestParam("token_ws") String token,
                                           @RequestHeader(value = "X-Actor", defaultValue = "SYSTEM_WEBPAY") String actor) {
        try {
            WebpayPlusTransactionCommitResponse response = webpayService.commitTransaction(token);

            if (response.getResponseCode() == 0 && "AUTHORIZED".equals(response.getStatus())) {
                Long reservationId = Long.parseLong(response.getBuyOrder().replace("ORD-", ""));

                // Transiciona el estado a CONFIRMADA, activando los eventos RabbitMQ y Kafka
                reservationService.updateStatus(reservationId, "CONFIRMADA", actor);

                return ResponseEntity.ok(Map.of(
                        "status", "APPROVED",
                        "reservationId", reservationId,
                        "amount", response.getAmount(),
                        "authorizationCode", response.getAuthorizationCode()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "REJECTED", "responseCode", response.getResponseCode()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error confirmando transacción: " + e.getMessage()));
        }
    }
}