package com.andesstay.msbff.controller;

import tools.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/audit")
public class AuditBffController {

    private final WebClient auditWebClient;

    public AuditBffController(WebClient auditWebClient) {
        this.auditWebClient = auditWebClient;
    }

    @GetMapping
    public Mono<JsonNode> list() {
        return auditWebClient.get()
                .uri("/api/audit")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    @GetMapping("/timeline/{aggregateId}")
    public Mono<JsonNode> timeline(@PathVariable String aggregateId) {
        return auditWebClient.get()
                .uri("/api/audit/timeline/{aggregateId}", aggregateId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}