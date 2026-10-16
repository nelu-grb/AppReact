package com.andesstay.msbff.controller;

import com.andesstay.msbff.dto.ReservationRequest;
import com.andesstay.msbff.dto.ReservationResponse;
import com.andesstay.msbff.dto.StatusUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/reservations")
public class ReservationBffController {

    private final WebClient reservationsWebClient;
    private final Validator validator;

    public ReservationBffController(WebClient reservationsWebClient, Validator validator) {
        this.reservationsWebClient = reservationsWebClient;
        this.validator = validator;
    }

    @PostMapping
    public Mono<ReservationResponse> create(@RequestBody ReservationRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        request.setOwnerOid(requiredClaim(jwt, "oid"));
        request.setGuestId(requiredClaim(jwt, "name"));
        request.setGuestEmail(requiredEmail(jwt));
        validate(request);

        return reservationsWebClient.post()
                .uri("/api/reservations")
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReservationResponse.class);
    }

    @GetMapping("/{id}")
    public Mono<ReservationResponse> getById(@PathVariable Long id,
                                             @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.get()
                .uri("/api/reservations/{id}", id)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(ReservationResponse.class);
    }

    @GetMapping
    public Mono<List<ReservationResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return reservationsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reservations")
                        .queryParamIfPresent("status", java.util.Optional.ofNullable(status))
                        .queryParamIfPresent("from", java.util.Optional.ofNullable(from))
                        .queryParamIfPresent("to", java.util.Optional.ofNullable(to))
                        .build())
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReservationResponse>>() {});
    }

    @PutMapping("/{id}/status")
    public Mono<ReservationResponse> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody StatusUpdateRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.put()
                .uri("/api/reservations/{id}/status", id)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReservationResponse.class);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id,
                             @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.delete()
                .uri("/api/reservations/{id}", id)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @GetMapping("/by-unit/{unitId}")
    public Mono<List<ReservationResponse>> getByUnit(@PathVariable Long unitId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.get()
                .uri("/api/reservations/by-unit/{unitId}", unitId)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReservationResponse>>() {});
    }

    @PostMapping("/{id}/pay")
    public Mono<ResponseEntity<Object>> payReservation(@PathVariable Long id,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.post()
                .uri("/api/reservations/{id}/pay", id)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .toEntity(Object.class);
    }

    @PostMapping("/confirm-payment")
    public Mono<ResponseEntity<Object>> confirmPayment(@RequestParam("token_ws") String tokenWs,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reservations/confirm-payment")
                        .queryParam("token_ws", tokenWs)
                        .build())
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .toEntity(Object.class)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class,
                        ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString())))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(400)
                        .body("{\"message\": \"No se pudo confirmar el pago con el servicio de reservas.\"}")));
    }

    private void validate(ReservationRequest request) {
        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }

    private String requiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The access token is missing the required " + claimName + " claim");
        }
        return value;
    }

    private String requiredEmail(Jwt jwt) {
        for (String claim : List.of("email", "preferred_username", "upn", "unique_name")) {
            String value = jwt.getClaimAsString(claim);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "The access token is missing an email or username claim");
    }
}