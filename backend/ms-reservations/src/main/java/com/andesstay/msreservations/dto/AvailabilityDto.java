package com.andesstay.msreservations.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvailabilityDto {
    private Long unitId;
    private String unitName;
    private LocalDate from;
    private LocalDate to;
    private Boolean available;
    private List<ReservationResponse> conflictingReservations;
}
