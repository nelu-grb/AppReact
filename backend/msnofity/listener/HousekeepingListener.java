package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import com.react.backend.msnofity.messaging.ReliableMessageHandler;
import com.react.backend.msnofity.service.HousekeepingService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio: housekeeping (cola de comandos q.cmd.housekeeping).
 */
@Component
public class HousekeepingListener {

    private final HousekeepingService housekeepingService;
    private final ReliableMessageHandler handler;

    public HousekeepingListener(HousekeepingService housekeepingService, ReliableMessageHandler handler) {
        this.housekeepingService = housekeepingService;
        this.handler = handler;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.housekeeping}")
    public void processHousekeeping(Message message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        handler.handle("housekeeping", message, channel, tag, envelope -> {
            JsonNode payload = envelope.path("payload");
            housekeepingService.createTicket(new HousekeepingTicketEvent(
                    envelope.path("correlationId").asText(),
                    payload.path("unitId").asText(),
                    payload.path("detail").asText(),
                    "NORMAL"));
        });
    }
}