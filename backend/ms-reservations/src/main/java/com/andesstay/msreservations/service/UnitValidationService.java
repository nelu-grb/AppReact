package com.andesstay.msreservations.service;

import com.andesstay.msreservations.dto.AvailabilityDto;
import com.andesstay.msreservations.dto.UnitDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitValidationService {

    private final WebClient catalogWebClient;

    public void validateUnitAvailability(Long unitId, LocalDate startDate, LocalDate endDate, Jwt jwt) {
        UnitDto unit = getUnit(unitId, jwt);
        if (unit == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe la unidad con id " + unitId
            );
        }

        AvailabilityDto availability = checkAvailability(unitId, startDate, endDate, jwt);
        if (!availability.getAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La unidad no está disponible entre " + startDate + " y " + endDate
            );
        }

        log.info("La unidad {} está disponible entre {} y {}", unitId, startDate, endDate);
    }

    public UnitDto getUnit(Long unitId, Jwt jwt) {
        try {
            return catalogWebClient.get()
                    .uri("/api/catalog/units/{id}", unitId)
                    .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .bodyToMono(UnitDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("No se encontró la unidad {} en el catálogo", unitId);
            return null;
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(), "El servicio de catálogo rechazó la solicitud de la unidad", e);
        } catch (Exception e) {
            log.error("No se pudo consultar la unidad {} en el catálogo", unitId, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "No se pudo consultar la unidad en el catálogo", e);
        }
    }

    private AvailabilityDto checkAvailability(Long unitId, LocalDate startDate, LocalDate endDate, Jwt jwt) {
        try {
            AvailabilityDto availability = catalogWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/catalog/units/{id}/availability")
                            .queryParam("from", startDate)
                            .queryParam("to", endDate)
                            .build(unitId))
                    .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .bodyToMono(AvailabilityDto.class)
                    .block();
            if (availability == null || availability.getAvailable() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "El catálogo no entregó la disponibilidad de la unidad");
            }
            return availability;
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(), "El servicio de catálogo rechazó la consulta de disponibilidad", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("No se pudo consultar la disponibilidad de la unidad {} en el catálogo", unitId, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "No se pudo consultar la disponibilidad de la unidad", e);
        }
    }
}
