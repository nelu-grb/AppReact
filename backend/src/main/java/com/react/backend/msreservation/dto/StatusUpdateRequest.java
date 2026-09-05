package com.react.backend.msreservation.dto;

import com.react.backend.msreservation.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "El estado es obligatorio")
    private ReservationStatus status;
}