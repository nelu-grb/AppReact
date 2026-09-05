package com.react.backend.msreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageEnvelope<T> {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private String type;
    @Builder.Default
    private Long timestamp = Instant.now().toEpochMilli();
    private String traceId;
    private String correlationId;
    private T payload;
}