package com.react.backend.msaudit.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;
    private String aggregateId;
    private String actor;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime timestamp;
}