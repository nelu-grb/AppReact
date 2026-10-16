package com.andesstay.msreservations.controller;

import com.andesstay.msreservations.dto.ReservationRequest;
import com.andesstay.msreservations.dto.ReservationResponse;
import com.andesstay.msreservations.dto.StatusUpdateRequest;
import com.andesstay.msreservations.model.ReservationStatus;
import com.andesstay.msreservations.service.ReservationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final Validator validator;

    public ReservationController(ReservationService reservationService, Validator validator) {
        this.reservationService = reservationService;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        setAuthenticatedUserIdentity(request, jwt);
        validate(request);

        ReservationResponse response = reservationService.createReservation(request, actor(jwt), jwt);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ReservationResponse response = isStaff(jwt)
                ? reservationService.getReservationById(id)
                : reservationService.getReservationByIdForOwner(id, requiredClaim(jwt, "oid"));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        ReservationResponse response = reservationService.updateStatus(id, request, actor(jwt));
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

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ReservationResponse> reservations = isStaff(jwt)
                ? reservationService.getReservations(status, from, to)
                : reservationService.getReservationsForOwner(requiredClaim(jwt, "oid"), status, from, to);
        return ResponseEntity.ok(reservations);
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

    private void setAuthenticatedUserIdentity(ReservationRequest request, Jwt jwt) {
        request.setOwnerOid(requiredClaim(jwt, "oid"));
        request.setGuestId(requiredClaim(jwt, "name"));
        request.setGuestEmail(requiredEmail(jwt));
    }

    private void validate(ReservationRequest request) {
        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }

    private boolean isStaff(Jwt jwt) {
        return hasAnyRole(jwt, "ADMIN", "RECEPCIONISTA", "OPERADOR");
    }

    private boolean hasAnyRole(Jwt jwt, String... expectedRoles) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return false;
        }
        List<String> expected = List.of(expectedRoles);
        return roles.stream()
                .map(role -> role.toUpperCase(java.util.Locale.ROOT))
                .anyMatch(expected::contains);
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