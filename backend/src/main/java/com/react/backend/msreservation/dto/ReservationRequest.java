package com.react.backend.msreservation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservationRequest {

    @NotNull(message = "El ID de la unidad es obligatorio")
    private Long unitId;

    @NotBlank(message = "El ID del huésped es obligatorio")
    private String guestId;

    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email del huésped es obligatorio")
    private String guestEmail;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a cero")
    private BigDecimal totalAmount;
} 