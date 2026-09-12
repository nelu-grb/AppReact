package com.andesstay.msbff.controller;

import com.andesstay.msbff.dto.AvailabilityResponse;
import com.andesstay.msbff.dto.UnitResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final WebClient catalogWebClient;

    public UnitController(WebClient catalogWebClient) {
        this.catalogWebClient = catalogWebClient;
    }

    @GetMapping
    public Mono<List<UnitResponse>> listAll() {
        return catalogWebClient.get()
                .uri("/api/units")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UnitResponse>>() {});
    }

    @GetMapping("/{id}")
    public Mono<UnitResponse> getById(@PathVariable Long id) {
        return catalogWebClient.get()
                .uri("/api/units/{id}", id)
                .retrieve()
                .bodyToMono(UnitResponse.class);
    }

    @GetMapping("/{id}/availability")
    public Mono<AvailabilityResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return catalogWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/units/{id}/availability")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(id))
                .retrieve()
                .bodyToMono(AvailabilityResponse.class);
    }
}
