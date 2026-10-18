package com.react.backend.msaudit.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
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

    public AuditEvent() {}

    public AuditEvent(Long id, String eventType, String aggregateId, String actor, String payload, LocalDateTime timestamp) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.actor = actor;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getActor() { return actor; }
    public String getPayload() { return payload; }
    public LocalDateTime getTimestamp() { return timestamp; }
}