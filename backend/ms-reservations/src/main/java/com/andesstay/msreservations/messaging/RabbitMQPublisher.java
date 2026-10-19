package com.andesstay.msreservations.messaging;

import com.andesstay.msreservations.dto.MessageEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.direct}")
    private String directExchange;

    @Value("${app.rabbitmq.exchange.topic}")
    private String topicExchange;

    @Value("${app.rabbitmq.routing.commands.housekeeping}")
    private String housekeepingRoutingKey;

    @Value("${app.rabbitmq.routing.commands.voucher}")
    private String voucherRoutingKey;

    @Value("${app.rabbitmq.routing.notifications.email}")
    private String notificationEmailRoutingKey;

    // deja el mensaje guardado en el broker hasta que se procese.
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
                topicExchange,
                notificationEmailRoutingKey,
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
                directExchange,
                housekeepingRoutingKey,
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
                directExchange,
                voucherRoutingKey,
                envelope,
                persistent(),
                new CorrelationData(correlationId)
        );
    }

}