package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.ReservationRequest;
import com.andesstay.msreservations.dto.ReservationResponse;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor", required = false) String actorHeader,
            @RequestHeader(value = "X-Actor-Name", required = false) String actorNameHeader) {
        String actor = actorHeader != null && !actorHeader.isBlank() ? actorHeader : actor(jwt);
        String actorName = actorNameHeader != null && !actorNameHeader.isBlank()
                ? actorNameHeader
                : actorDisplayName(jwt, actor);
        ReservationResponse response = reservationService.createReservation(request, actor, actorName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor", required = false) String actorHeader,
            @RequestHeader(value = "X-Actor-Name", required = false) String actorNameHeader) {
        String actor = actorHeader != null && !actorHeader.isBlank() ? actorHeader : actor(jwt);
        String actorName = actorNameHeader != null && !actorNameHeader.isBlank()
                ? actorNameHeader
                : actorDisplayName(jwt, actor);
        ReservationResponse response = reservationService.updateStatus(id, request, actor, actorName);
        return ResponseEntity.ok(response);
    }

    private String actor(Jwt jwt) {
        if (jwt == null) return "ANONYMOUS";

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) return preferredUsername;

        String uniqueName = jwt.getClaimAsString("unique_name");
        if (uniqueName != null && !uniqueName.isBlank()) return uniqueName;

        String name = jwt.getClaimAsString("name");
        return name != null && !name.isBlank() ? name : jwt.getSubject();
    }

    private String actorDisplayName(Jwt jwt, String actor) {
        if (jwt == null) return actor;
        String name = jwt.getClaimAsString("name");
        return name != null && !name.isBlank() ? name : actor;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reservationService.getReservations(status, from, to));
    }

    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<List<ReservationResponse>> getByUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(reservationService.getReservationsByUnit(unitId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCancelled(@PathVariable Long id) {
        reservationService.deleteCancelledReservation(id);
        return ResponseEntity.noContent().build();
    }
}