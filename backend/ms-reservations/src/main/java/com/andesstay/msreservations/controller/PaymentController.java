package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.PaymentInitResponseDto;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.security.AuthenticatedUser;
import com.andesstay.msreservations.service.ReservationService;
import com.andesstay.msreservations.service.WebpayService;
import com.andesstay.msreservations.service.WebpayService.WebpayCommitResponse;
import com.andesstay.msreservations.service.WebpayService.WebpayCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final WebpayService webpayService;
    private final ReservationService reservationService;

    @Value("${app.payment-result-url:http://localhost:5173/payment-result}")
    private String paymentResultUrl;

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> iniciarPago(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Reservation reservation = reservationService.getReservationForUser(
                id, AuthenticatedUser.from(jwt));
        if (reservation.getStatus() != ReservationStatus.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reserva no está disponible para pago");
        }

        try {
            String sessionId = "SESS-" + reservation.getGuestId();
            WebpayCreateResponse response = webpayService.createTransaction(
                    reservation.getId(),
                    sessionId,
                    reservation.getTotalAmount()
            );

            return ResponseEntity.ok(new PaymentInitResponseDto(response.getUrl(), response.getToken()));
        } catch (Exception e) {
            log.error("No se pudo iniciar el pago de la reserva {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo iniciar el pago. Inténtalo de nuevo."));
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Void> confirmarPago(
            @RequestParam(value = "token_ws", required = false) String token,
            @RequestParam(value = "TBK_TOKEN", required = false) String cancelledToken) {
        if (token == null || token.isBlank()) {
            return redirectToPaymentResult(
                    cancelledToken == null || cancelledToken.isBlank() ? "error" : "cancelled",
                    null);
        }

        try {
            WebpayCommitResponse response = webpayService.commitTransaction(token);

            if (response == null
                    || response.getResponseCode() != 0
                    || !"AUTHORIZED".equals(response.getStatus())) {
                log.warn("Webpay rechazó el pago (responseCode={})",
                        response == null ? "sin respuesta" : response.getResponseCode());
                return redirectToPaymentResult("rejected", null);
            }

            Long reservationId = parseReservationId(response.getBuyOrder());
            reservationService.confirmWebpayPayment(reservationId, response.getAmount());
            return redirectToPaymentResult("success", reservationId);
        } catch (Exception e) {
            log.error("No se pudo confirmar el pago en Webpay", e);
            return redirectToPaymentResult("error", null);
        }
    }

    private Long parseReservationId(String buyOrder) {
        if (buyOrder == null || !buyOrder.matches("ORD-[1-9][0-9]*")) {
            throw new IllegalArgumentException("Webpay devolvió un buy_order inválido");
        }
        return Long.parseLong(buyOrder.substring("ORD-".length()));
    }

    private ResponseEntity<Void> redirectToPaymentResult(String status, Long reservationId) {
        UriComponentsBuilder location = UriComponentsBuilder.fromUriString(paymentResultUrl)
                .queryParam("status", status);
        if (reservationId != null) {
            location.queryParam("id", reservationId);
        }
        URI redirectUri = location.build().encode().toUri();
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(redirectUri).build();
    }

}