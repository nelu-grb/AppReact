package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.PaymentInitResponseDto;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.model.Reservation;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.security.AuthenticatedUser;
import com.andesstay.msreservations.service.ReservationService;
import com.andesstay.msreservations.service.WebpayService;
import com.andesstay.msreservations.service.WebpayService.WebpayCommitResponse;
import com.andesstay.msreservations.service.WebpayService.WebpayCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class PaymentController {

    private final WebpayService webpayService;
    private final ReservationService reservationService;

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> iniciarPago(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Reservation reservation = reservationService.getReservationForUser(
                id, AuthenticatedUser.from(jwt));

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

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmarPago(@RequestParam("token_ws") String token,
                                           @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        try {
            WebpayCommitResponse response = webpayService.commitTransaction(token);

            if (response.getResponseCode() == 0 && "AUTHORIZED".equals(response.getStatus())) {
                Long reservationId = Long.parseLong(response.getBuyOrder().replace("ORD-", ""));
                reservationService.getReservationForUser(reservationId, user);

                StatusUpdateRequest statusRequest = new StatusUpdateRequest();
                statusRequest.setStatus(ReservationStatus.CONFIRMADA);

                reservationService.updateStatus(reservationId, statusRequest, user.actor());

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
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error confirmando transacción: " + e.getMessage()));
        }
    }

}