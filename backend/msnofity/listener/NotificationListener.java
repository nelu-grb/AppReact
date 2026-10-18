package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.dto.NotificationEvent;
import com.react.backend.msnofity.messaging.ManualAckMessageProcessor;
import com.react.backend.msnofity.service.NotificationService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final ManualAckMessageProcessor messageProcessor;

    public NotificationListener(
            NotificationService notificationService,
            ObjectMapper objectMapper,
            ManualAckMessageProcessor messageProcessor) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.messageProcessor = messageProcessor;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.email}")
    public void processNotification(Message message, Channel channel) throws IOException {
        messageProcessor.process("notificación", channel, message.getMessageProperties().getDeliveryTag(), () -> {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            String type = envelope.path("type").asText();
            JsonNode payload = envelope.path("payload");
            NotificationEvent event = new NotificationEvent(
                    type.replace("_SEND", ""),
                    payload.path("email").asText(),
                    payload.path("subject").asText(),
                    payload.path("body").asText());
            if ("EMAIL".equalsIgnoreCase(event.type())) {
                notificationService.sendEmail(event);
            } else if ("WEBPUSH".equalsIgnoreCase(event.type())) {
                notificationService.sendWebPush(event);
            } else {
                throw new IllegalArgumentException("Notificación no soportada: " + type);
            }
            return null;
        });
    }
}
