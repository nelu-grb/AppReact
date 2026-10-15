package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.dto.NotificationEvent;
import com.react.backend.msnofity.exception.NonRecoverableMessageException;
import com.react.backend.msnofity.messaging.ReliableMessageHandler;
import com.react.backend.msnofity.service.NotificationService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio: notificaciones (cola de comandos q.cmd.email).
 * El ACK, los reintentos y el NACK a la DLQ los resuelve ReliableMessageHandler.
 */
@Component
public class NotificationListener {

    private final NotificationService notificationService;
    private final ReliableMessageHandler handler;

    public NotificationListener(NotificationService notificationService, ReliableMessageHandler handler) {
        this.notificationService = notificationService;
        this.handler = handler;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.email}")
    public void processEmail(Message message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        handler.handle("email", message, channel, tag, envelope -> {
            JsonNode payload = envelope.path("payload");
            NotificationEvent event = new NotificationEvent(
                    envelope.path("type").asText("EMAIL_SEND").replace("_SEND", ""),
                    payload.path("email").asText(),
                    payload.path("subject").asText(),
                    payload.path("body").asText());

            if ("EMAIL".equalsIgnoreCase(event.type())) {
                notificationService.sendEmail(event);
            } else if ("WEBPUSH".equalsIgnoreCase(event.type())) {
                notificationService.sendWebPush(event);
            } else {
                // Antes se confirmaba con ACK y el mensaje se perdía en silencio;
                // ahora es un error no recuperable y termina en la DLQ.
                throw new NonRecoverableMessageException(
                        "Tipo de notificación no soportado: " + event.type());
            }
        });
    }
}