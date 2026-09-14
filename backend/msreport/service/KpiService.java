package com.react.backend.msreport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.react.backend.msreport.model.Report;
import com.react.backend.msreport.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class KpiService {

    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    public KpiService(ReportRepository reportRepository, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getKpis() {
        List<JsonNode> events = readReservationEvents();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reservationsByHour", reservationsByHour(events));
        result.put("activeOccupancy", activeOccupancy(events));
        result.put("averageCycleTimeMinutes", averageCycleTimeMinutes(events));
        return result;
    }

    public Map<String, Long> reservationsByHour() {
        return reservationsByHour(readReservationEvents());
    }

    public long activeOccupancy() {
        return activeOccupancy(readReservationEvents());
    }

    public double averageCycleTimeMinutes() {
        return averageCycleTimeMinutes(readReservationEvents());
    }

    private List<JsonNode> readReservationEvents() {
        return reportRepository.findAll().stream()
                .filter(report -> "RESERVATION_EVENT".equals(report.getEventType()))
                .map(Report::getDescription)
                .map(this::parse)
                .filter(event -> event != null && event.has("reservationId"))
                .toList();
    }

    private JsonNode parse(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception exception) {
            return null;
        }
    }

    private Map<String, Long> reservationsByHour(List<JsonNode> events) {
        Map<String, Long> counts = new TreeMap<>();
        events.stream()
                .filter(event -> "RESERVATION_CREATED".equals(event.path("eventType").asText()))
                .forEach(event -> {
                    String hour = Instant.ofEpochMilli(event.path("timestamp").asLong())
                            .atZone(ZoneId.systemDefault())
                            .withMinute(0).withSecond(0).withNano(0)
                            .toString();
                    counts.merge(hour, 1L, Long::sum);
                });
        return counts;
    }

    private long activeOccupancy(List<JsonNode> events) {
        Map<String, JsonNode> latestByReservation = events.stream()
                .filter(event -> event.has("reservationId"))
                .collect(Collectors.toMap(
                        event -> event.path("reservationId").asText(),
                        event -> event,
                        (first, second) -> first.path("timestamp").asLong() >= second.path("timestamp").asLong()
                                ? first : second));
        return latestByReservation.values().stream()
                .filter(event -> "EN_ESTADIA".equals(event.path("status").asText()))
                .count();
    }

    private double averageCycleTimeMinutes(List<JsonNode> events) {
        return events.stream()
                .filter(event -> event.hasNonNull("createdAt") && event.hasNonNull("updatedAt"))
                .mapToLong(event -> cycleMinutes(event.path("createdAt").asText(), event.path("updatedAt").asText()))
                .filter(minutes -> minutes >= 0)
                .average()
                .orElse(0D);
    }

    private long cycleMinutes(String createdAt, String updatedAt) {
        try {
            return Duration.between(LocalDateTime.parse(createdAt), LocalDateTime.parse(updatedAt)).toMinutes();
        } catch (Exception exception) {
            return -1;
        }
    }
}
