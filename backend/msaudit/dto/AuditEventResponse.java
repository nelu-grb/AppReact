package com.react.backend.msaudit.dto;


import java.time.LocalDateTime;

public class AuditEventResponse {
    private Long id;
    private String eventType;
    private String aggregateId;
    private String actor;
    private String payload;
    private LocalDateTime timestamp;

    public AuditEventResponse(Long id, String eventType, String aggregateId, String actor, String payload, LocalDateTime timestamp) {
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