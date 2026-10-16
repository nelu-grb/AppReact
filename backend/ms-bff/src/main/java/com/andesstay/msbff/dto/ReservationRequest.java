package com.andesstay.msbff.dto;

import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;


@Data
public class ReservationRequest {

    @NotNull(message = "El ID de la unidad es obligatorio")
    private Long unitId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;
}