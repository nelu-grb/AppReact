package com.andesstay.mscatalog.dto;

import java.time.LocalDate;
import java.util.List;

public class AvailabilityResponse {
    private final Long unitId;
    private final String unitName;
    private final LocalDate from;
    private final LocalDate to;
    private final Boolean available;
    private final List<Object> conflictingReservations;

    public AvailabilityResponse(Long unitId, String unitName, LocalDate from, LocalDate to,
                                Boolean available, List<Object> conflictingReservations) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.from = from;
        this.to = to;
        this.available = available;
        this.conflictingReservations = conflictingReservations;
    }

    public Long getUnitId() { return unitId; }
    public String getUnitName() { return unitName; }
    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }
    public Boolean getAvailable() { return available; }
    public List<Object> getConflictingReservations() { return conflictingReservations; }
}
