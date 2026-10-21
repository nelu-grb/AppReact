package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.ReservationRequest;
import com.andesstay.msreservations.dto.ReservationResponse;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.security.AuthenticatedUser;
import com.andesstay.msreservations.service.ReservationService;
import jakarta.validation.Valid;
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
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        ReservationResponse response = reservationService.createReservation(request, user, jwt);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reservationService.getReservationByIdForUser(
                id, AuthenticatedUser.from(jwt)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        return ResponseEntity.ok(reservationService.updateStatus(id, request, user.actor()));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reservationService.getReservationsForUser(
                AuthenticatedUser.from(jwt), status, from, to));
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
