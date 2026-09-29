package com.andesstay.msreservations.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPublisherCallbacks {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublisherCallbacks.class);

    public RabbitPublisherCallbacks(RabbitTemplate rabbitTemplate) {

        // El broker responde ACK o NACK por cada mensaje publicado
        rabbitTemplate.setConfirmCallback((correlation, ack, cause) -> {
            String id = (correlation != null) ? correlation.getId() : "sin-id";
            if (ack) {
                log.info("[CONFIRM] Broker confirmó el mensaje {}", id);
            } else {
                log.error("[CONFIRM] Broker RECHAZÓ el mensaje {}: {}", id, cause);
            }
        });

        // El mensaje llegó al Exchange pero NO PUDO enrutarse a ninguna cola
        rabbitTemplate.setReturnsCallback(returned ->
            log.error("[RETURN] Mensaje NO ENRUTADO exchange={} routingKey={} motivo={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
    }
}