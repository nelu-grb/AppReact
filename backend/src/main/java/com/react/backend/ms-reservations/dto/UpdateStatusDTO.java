package com.andesstay.reservations.dto;

import com.andesstay.reservations.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusDTO {
    @NotNull(message = "El status es obligatorio")
    private ReservationStatus status;
}