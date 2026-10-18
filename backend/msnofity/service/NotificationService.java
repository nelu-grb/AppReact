package com.react.backend.msnofity.service;

import com.react.backend.msnofity.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RestClient resendClient;
    private final String fromEmail;

    public NotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail) {
        this.resendClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.fromEmail = fromEmail;
    }

    public void sendEmail(NotificationEvent event) {
        log.info("Procesando EMAIL -> Para: {} | Asunto: {}", event.recipient(), event.subject());
        resendClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", fromEmail,
                        "to", event.recipient(),
                        "subject", event.subject(),
                        "text", event.message()))
                .retrieve()
                .toBodilessEntity();
        log.info("Email enviado mediante Resend -> Para: {}", event.recipient());
    }

    public void sendWebPush(NotificationEvent event) {
        log.info("Procesando WEBPUSH -> Para: {} | Mensaje: {}", event.recipient(), event.message());
    }
}