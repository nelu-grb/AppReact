package com.andesstay.msbff.controller;

import tools.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reports")
public class ReportBffController {

    private final WebClient reportWebClient;

    public ReportBffController(WebClient reportWebClient) {
        this.reportWebClient = reportWebClient;
    }

    @GetMapping
    public Mono<JsonNode> list() {
        return get("/api/reports");
    }

    @GetMapping("/kpis")
    public Mono<JsonNode> kpis() {
        return get("/api/reports/kpis");
    }

    @GetMapping("/kpis/reservations-by-hour")
    public Mono<JsonNode> reservationsByHour() {
        return get("/api/reports/kpis/reservations-by-hour");
    }

    @GetMapping("/kpis/active-occupancy")
    public Mono<JsonNode> activeOccupancy() {
        return get("/api/reports/kpis/active-occupancy");
    }

    @GetMapping("/kpis/cycle-time")
    public Mono<JsonNode> cycleTime() {
        return get("/api/reports/kpis/cycle-time");
    }

    private Mono<JsonNode> get(String path) {
        return reportWebClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}