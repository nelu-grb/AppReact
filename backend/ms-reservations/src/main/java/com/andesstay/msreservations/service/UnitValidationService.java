package com.andesstay.msreservations.service;

import com.andesstay.msreservations.dto.AvailabilityDto;
import com.andesstay.msreservations.dto.UnitDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitValidationService {

    private final WebClient catalogWebClient;

    /**
     * Validates that a unit exists and is available for the given dates
     * @param unitId the unit ID to validate
     * @param startDate reservation start date
     * @param endDate reservation end date
     * @throws IllegalArgumentException if unit doesn't exist or is not available
     */
    public void validateUnitAvailability(Long unitId, LocalDate startDate, LocalDate endDate) {
        // First, verify the unit exists
        UnitDto unit = getUnit(unitId);
        if (unit == null) {
            throw new IllegalArgumentException("Unidad no existe con ID: " + unitId);
        }

        AvailabilityDto availability = checkAvailability(unitId, startDate, endDate);
        if (!availability.getAvailable()) {
            throw new IllegalArgumentException(
                    "Unidad no está disponible para las fechas solicitadas: " + startDate + " a " + endDate
            );
        }

        log.info("Unit {} is available for reservation from {} to {}", unitId, startDate, endDate);
    }

    private UnitDto getUnit(Long unitId) {
        try {
            return catalogWebClient.get()
                    .uri("/api/units/{id}", unitId)
                    .retrieve()
                    .bodyToMono(UnitDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Unit {} not found in catalog", unitId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching unit {} from catalog", unitId, e);
            throw new RuntimeException("Error validating unit with catalog service", e);
        }
    }

    private AvailabilityDto checkAvailability(Long unitId, LocalDate startDate, LocalDate endDate) {
        try {
            return catalogWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/units/{id}/availability")
                            .queryParam("from", startDate)
                            .queryParam("to", endDate)
                            .build(unitId))
                    .retrieve()
                    .bodyToMono(AvailabilityDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Error checking availability for unit {} from catalog", unitId, e);
            throw new RuntimeException("Error checking unit availability with catalog service", e);
        }
    }
}
