package com.react.backend.msreservation.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "cmd.direct";
    private static final String ROUTING_KEY_EMAIL = "email.send";
    private static final String ROUTING_KEY_HOUSEKEEPING = "housekeeping.ticket";
    private static final String ROUTING_KEY_VOUCHER = "voucher.gen";

    public void publishEmailCommand(String recipient, String subject, String body, String correlationId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "EMAIL");
        payload.put("recipient", recipient);
        payload.put("subject", subject);
        payload.put("message", body);
        payload.put("correlationId", correlationId);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_EMAIL, payload);
    }

    public void publishHousekeepingTicket(Object unitId, String description, String correlationId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", correlationId);
        payload.put("roomId", String.valueOf(unitId));
        payload.put("taskType", description);
        payload.put("priority", "ALTA");

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_HOUSEKEEPING, payload);
    }

    public void publishVoucherGenCommand(Long reservationId, String correlationId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", String.valueOf(reservationId));
        payload.put("customerEmail", "");
        payload.put("voucherCode", "VOUCHER-" + reservationId);
        payload.put("amount", 0.0);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_VOUCHER, payload);
    }
}