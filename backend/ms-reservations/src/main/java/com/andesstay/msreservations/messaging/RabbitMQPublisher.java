package com.andesstay.msreservations.messaging;

import com.andesstay.msreservations.config.RabbitMQConfig;
import com.andesstay.msreservations.dto.MessageEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEmailCommand(String guestEmail, String subject, String body, String correlationId) {
        MessageEnvelope<Map<String, String>> envelope = MessageEnvelope.<Map<String, String>>builder()
                .type("EMAIL_SEND")
                .traceId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .payload(Map.of("email", guestEmail, "subject", subject, "body", body))
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, "email.send", envelope);
    }

    public void publishHousekeepingTicket(Long unitId, String detail, String correlationId) {
        MessageEnvelope<Map<String, Object>> envelope = MessageEnvelope.<Map<String, Object>>builder()
                .type("HOUSEKEEPING_TICKET")
                .traceId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .payload(Map.of("unitId", unitId, "detail", detail))
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, "housekeeping.ticket", envelope);
    }

    public void publishVoucherGenCommand(Long reservationId, String correlationId) {
        MessageEnvelope<Map<String, Object>> envelope = MessageEnvelope.<Map<String, Object>>builder()
                .type("VOUCHER_GEN")
                .traceId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .payload(Map.of("reservationId", reservationId))
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, "voucher.gen", envelope);
    }
}