package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import com.react.backend.msnofity.messaging.ManualAckMessageProcessor;
import com.react.backend.msnofity.service.HousekeepingService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HousekeepingListener {

    private final HousekeepingService housekeepingService;
    private final ObjectMapper objectMapper;
    private final ManualAckMessageProcessor messageProcessor;

    public HousekeepingListener(
            HousekeepingService housekeepingService,
            ObjectMapper objectMapper,
            ManualAckMessageProcessor messageProcessor) {
        this.housekeepingService = housekeepingService;
        this.objectMapper = objectMapper;
        this.messageProcessor = messageProcessor;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.housekeeping}")
    public void processHousekeeping(Message message, Channel channel) throws IOException {
        messageProcessor.process("housekeeping", channel, message.getMessageProperties().getDeliveryTag(), () -> {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            JsonNode payload = envelope.path("payload");
            housekeepingService.createTicket(new HousekeepingTicketEvent(
                    envelope.path("correlationId").asText(),
                    payload.path("unitId").asText(),
                    payload.path("detail").asText(),
                    "NORMAL"));
            return null;
        });
    }
}
