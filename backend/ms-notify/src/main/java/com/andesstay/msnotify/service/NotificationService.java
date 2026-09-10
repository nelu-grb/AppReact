package com.react.backend.msnofity.service;

import com.react.backend.msnofity.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendEmail(NotificationEvent event) {
        log.info("Procesando EMAIL -> Para: {} | Asunto: {}", event.recipient(), event.subject());
    }

    public void sendWebPush(NotificationEvent event) {
        log.info("Procesando WEBPUSH -> Para: {} | Mensaje: {}", event.recipient(), event.message());
    }
}