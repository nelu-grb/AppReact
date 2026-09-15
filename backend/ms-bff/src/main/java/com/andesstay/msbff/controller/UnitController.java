package com.andesstay.msbff.controller;

import com.andesstay.msbff.dto.AvailabilityResponse;
import com.andesstay.msbff.dto.UnitRequest;
import com.andesstay.msbff.dto.UnitResponse;
import com.andesstay.msbff.dto.UnitType;
import jakarta.validation.Valid;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PostMapping
    public Mono<UnitResponse> create(@Valid @RequestBody UnitRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return catalogWebClient.post()
                .uri("/api/catalog/units")
                .header("X-Actor", actorName(jwt))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UnitResponse.class);
    }

    @GetMapping
    public Mono<List<UnitResponse>> listAll(
            @RequestParam(required = false) UnitType type,
            @RequestParam(required = false) Boolean availability) {
        return catalogWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/catalog/units");
                    if (type != null) builder.queryParam("type", type);
                    if (availability != null) builder.queryParam("availability", availability);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UnitResponse>>() {});
    }

    @GetMapping("/{id}")
    public Mono<UnitResponse> getById(@PathVariable Long id) {
        return catalogWebClient.get()
                .uri("/api/catalog/units/{id}", id)
                .retrieve()
                .bodyToMono(UnitResponse.class);
    }

    @PutMapping("/{id}")
    public Mono<UnitResponse> update(@PathVariable Long id,
                                    @Valid @RequestBody UnitRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return catalogWebClient.put()
                .uri("/api/catalog/units/{id}", id)
                .header("X-Actor", actorName(jwt))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UnitResponse.class);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id,
                            @AuthenticationPrincipal Jwt jwt) {
        return catalogWebClient.delete()
                .uri("/api/catalog/units/{id}", id)
                .header("X-Actor", actorName(jwt))
                .retrieve()
                .bodyToMono(Void.class);
    }

    private String actorName(Jwt jwt) {
        if (jwt == null) return "ANONYMOUS";

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) return preferredUsername;

        String uniqueName = jwt.getClaimAsString("unique_name");
        if (uniqueName != null && !uniqueName.isBlank()) return uniqueName;

        String name = jwt.getClaimAsString("name");
        return name != null && !name.isBlank() ? name : jwt.getSubject();
    }

    @GetMapping("/{id}/availability")
    public Mono<AvailabilityResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return catalogWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/catalog/units/{id}/availability")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(id))
                .retrieve()
                .bodyToMono(AvailabilityResponse.class);
    }
}
