package com.andesstay.reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvelopeDTO<T> {
    private String type;
    private String eventId;
    private String timestamp;
    private String traceId;
    private String correlationId;
    private T payload;
}