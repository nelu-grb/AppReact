package com.andesstay.msreservations.dto;

import com.andesstay.msreservations.model.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private Long unitId;
    private String guestId;
    private String guestEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}