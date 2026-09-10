package com.andesstay.msbff.controller;

import com.andesstay.msbff.dto.ReservationRequest;
import com.andesstay.msbff.dto.ReservationResponse;
import com.andesstay.msbff.dto.StatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationBffController {

    private final WebClient reservationsWebClient;


    public ReservationBffController(WebClient reservationsWebClient) {
        this.reservationsWebClient = reservationsWebClient;
    }

    @PostMapping
    public Mono<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.post()
                .uri("/api/reservations")
                .header("X-Actor", jwt.getSubject())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReservationResponse.class);
    }

    @GetMapping("/{id}")
    public Mono<ReservationResponse> getById(@PathVariable Long id) {
        return reservationsWebClient.get()
                .uri("/api/reservations/{id}", id)
                .retrieve()
                .bodyToMono(ReservationResponse.class);
    }

    @GetMapping
    public Mono<List<ReservationResponse>> list(
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
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReservationResponse>>() {});
    }

    @PutMapping("/{id}/status")
    public Mono<ReservationResponse> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody StatusUpdateRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return reservationsWebClient.put()
                .uri("/api/reservations/{id}/status", id)
                .header("X-Actor", jwt.getSubject())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReservationResponse.class);
    }
}