package com.andesstay.msreservations.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MessageEnvelope<T> implements Serializable {

    private String traceId;
    private String correlationId;
    private String eventType;
    private String source;
    private LocalDateTime timestamp;
    private T payload;

    public MessageEnvelope() {
    }

    public MessageEnvelope(String traceId, String correlationId, String eventType, String source, LocalDateTime timestamp, T payload) {
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.eventType = eventType;
        this.source = source;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public static <T> MessageEnvelopeBuilder<T> builder() {
        return new MessageEnvelopeBuilder<>();
    }

    public static class MessageEnvelopeBuilder<T> {
        private String traceId;
        private String correlationId;
        private String eventType;
        private String source;
        private LocalDateTime timestamp;
        private T payload;

        public MessageEnvelopeBuilder<T> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public MessageEnvelopeBuilder<T> correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public MessageEnvelopeBuilder<T> type(String type) {
            this.eventType = type;
            return this;
        }

        public MessageEnvelopeBuilder<T> eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public MessageEnvelopeBuilder<T> source(String source) {
            this.source = source;
            return this;
        }

        public MessageEnvelopeBuilder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MessageEnvelopeBuilder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public MessageEnvelope<T> build() {
            return new MessageEnvelope<>(traceId, correlationId, eventType, source, timestamp, payload);
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getType() {
        return eventType;
    }

    public void setType(String type) {
        this.eventType = type;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}