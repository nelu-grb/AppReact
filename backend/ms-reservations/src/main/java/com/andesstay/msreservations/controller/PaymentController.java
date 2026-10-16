package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.PaymentInitResponseDto;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Map;
import java.util.Locale;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class PaymentController {

    private final WebpayService webpayService;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> iniciarPago(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Reservation reservation = isGuest(jwt)
                ? reservationRepository.findByIdAndOwnerOid(id, requiredClaim(jwt, "oid"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"))
                : reservationRepository.findById(id)
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

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmarPago(@RequestParam("token_ws") String token,
                                           @AuthenticationPrincipal Jwt jwt) {
        String actor = actor(jwt);
        try {
            WebpayCommitResponse response = webpayService.commitTransaction(token);

            if (response.getResponseCode() == 0 && "AUTHORIZED".equals(response.getStatus())) {
                Long reservationId = Long.parseLong(response.getBuyOrder().replace("ORD-", ""));
                if (isGuest(jwt)) {
                    reservationService.getReservationByIdForOwner(reservationId, requiredClaim(jwt, "oid"));
                }

                StatusUpdateRequest statusRequest = new StatusUpdateRequest();
                statusRequest.setStatus(ReservationStatus.CONFIRMADA);

                reservationService.updateStatus(reservationId, statusRequest, actor);

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

    private boolean isGuest(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return false;
        }
        return roles.stream()
                .map(role -> Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toUpperCase(Locale.ROOT))
                .anyMatch(role -> role.equals("GUEST") || role.equals("GUEST_USER") || role.equals("HUESPED"));
    }

    private String requiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The access token is missing the required " + claimName + " claim");
        }
        return value;
    }

    private String actor(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) return preferredUsername;

        String uniqueName = jwt.getClaimAsString("unique_name");
        if (uniqueName != null && !uniqueName.isBlank()) return uniqueName;

        String name = jwt.getClaimAsString("name");
        return name != null && !name.isBlank() ? name : jwt.getSubject();
    }
}