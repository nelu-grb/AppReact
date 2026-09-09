package com.react.backend.msaudit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
    private Long id;
    private String eventType;
    private String aggregateId;
    private String actor;
    private String payload;
    private LocalDateTime timestamp;
}