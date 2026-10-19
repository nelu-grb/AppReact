package com.andesstay.msbff.config;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

// propaga a quien consume el BFF el mismo código y mensaje que respondió el servicio interno;
// si el servicio está caído, responde 502 en lugar de un 500 genérico.
@RestControllerAdvice
public class DownstreamExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleDownstreamStatus(WebClientResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(exception.getResponseBodyAsString());
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<Map<String, String>> handleDownstreamDown(WebClientRequestException exception) {
        return ResponseEntity.status(502)
                .body(Map.of("message", "Un servicio interno no está disponible"));
    }
}
