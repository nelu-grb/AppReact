package com.andesstay.reservations.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateReservationDTO {
    @NotNull(message = "El guestId es obligatorio")
    private String guestId;

    @NotNull(message = "El unitId es obligatorio")
    private Long unitId;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private BigDecimal totalAmount;
}