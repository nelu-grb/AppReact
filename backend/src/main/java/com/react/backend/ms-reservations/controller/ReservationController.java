package com.andesstay.reservations.controller;

import com.andesstay.reservations.dto.CreateReservationDTO;
import com.andesstay.reservations.dto.UpdateStatusDTO;
import com.andesstay.reservations.model.Reservation;
import com.andesstay.reservations.model.ReservationStatus;
import com.andesstay.reservations.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Reservation> create(@Valid @RequestBody CreateReservationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusDTO dto,
            Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "system";
        return ResponseEntity.ok(reservationService.updateStatus(id, dto.getStatus(), actor));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> search(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reservationService.searchReservations(status, from, to));
    }
}