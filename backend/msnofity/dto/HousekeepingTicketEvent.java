package com.react.backend.msnofity.dto;

public record HousekeepingTicketEvent(
    String reservationId,
    String roomId,
    String taskType,
    String priority
) {}