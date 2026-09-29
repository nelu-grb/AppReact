package com.andesstay.msreservations.messaging;

import com.andesstay.msreservations.config.RabbitMQConfig;
import com.andesstay.msreservations.dto.MessageEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Método auxiliar para forzar persistencia en disco por mensaje
    private MessagePostProcessor persistent() {
        return message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        };
    }

    public void publishEmailCommand(String guestEmail, String subject, String body, String correlationId) {
        MessageEnvelope<Map<String, String>> envelope = MessageEnvelope.<Map<String, String>>builder()
                .type("EMAIL_SEND")
                .traceId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .payload(Map.of("email", guestEmail, "subject", subject, "body", body))
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT, 
                "email.send", 
                envelope,
                persistent(),
                new CorrelationData(correlationId)
        );
    }

    public void publishHousekeepingTicket(Long unitId, String detail, String correlationId) {
        MessageEnvelope<Map<String, Object>> envelope = MessageEnvelope.<Map<String, Object>>builder()
                .type("HOUSEKEEPING_TICKET")
                .traceId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .payload(Map.of("unitId", unitId, "detail", detail))
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT, 
                "housekeeping.ticket", 
                envelope,
                persistent(),
                new CorrelationData(correlationId)
        );
    }

    public void publishVoucherGenCommand(Long reservationId, String guestEmail, java.math.BigDecimal totalAmount, String correlationId) {
        MessageEnvelope<Map<String, Object>> envelope = MessageEnvelope.<Map<String, Object>>builder()
                .type("VOUCHER_GEN")
                .traceId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .payload(Map.of(
                        "reservationId", reservationId,
                        "customerEmail", guestEmail,
                        "voucherCode", "VOUCHER-" + reservationId,
                        "amount", totalAmount
                ))
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT, 
                "voucher.gen", 
                envelope,
                persistent(),
                new CorrelationData(correlationId)
        );
    }

    // Nuevo método para publicar evento Pub/Sub al exchange Fanout
    public void publishReservationConfirmed(Long reservationId, Map<String, Object> payload) {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("eventId", eventId);
        envelope.put("type", "RESERVATION_CONFIRMED");
        envelope.put("timestamp", System.currentTimeMillis());
        envelope.put("correlationId", "RES-" + reservationId);
        envelope.put("payload", payload);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVT_EXCHANGE,
                "", // En fanout la routing key se ignora
                envelope,
                persistent(),
                new CorrelationData(eventId)
        );
    }
}