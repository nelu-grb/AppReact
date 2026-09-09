package com.andesstay.msreservations.dto;

import com.andesstay.msreservations.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "El estado es obligatorio")
    private ReservationStatus status;
}