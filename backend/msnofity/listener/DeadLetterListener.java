package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Consume las tres DLQ (email, housekeeping y voucher) y deja constancia en logs
 * de cada mensaje no entregado: de qué cola venía, por qué murió y qué contenía.
 *
 * El motivo (reason) lo agrega RabbitMQ en el header x-death:
 *   rejected = NACK sin requeue | expired = venció el TTL | maxlen = desbordó el límite de la cola
 *
 * Después de registrar, el mensaje se confirma (ACK) y sale de la DLQ.
 * Si se quiere que se acumulen para mostrar la métrica "mensajes en DLQ" en el
 * Management UI, basta con poner app.rabbitmq.dlq.listener.enabled=false.
 */
@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);
    private static final int MAX_PAYLOAD_CHARS = 1000;

    private final ObjectMapper objectMapper;

    public DeadLetterListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            queues = {
                    "${app.rabbitmq.queue.email-dlq}",
                    "${app.rabbitmq.queue.housekeeping-dlq}",
                    "${app.rabbitmq.queue.voucher-dlq}"
            },
            autoStartup = "${app.rabbitmq.dlq.listener.enabled:true}")
    public void onDeadLetter(Message message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            MessageProperties props = message.getMessageProperties();

            String sourceQueue = "-";
            String reason = "-";
            Object deathCount = "-";
            List<Map<String, ?>> xDeath = props.getXDeathHeader();
            if (xDeath != null && !xDeath.isEmpty()) {
                Map<String, ?> firstDeath = xDeath.get(0);
                sourceQueue = String.valueOf(firstDeath.get("queue"));
                reason = String.valueOf(firstDeath.get("reason"));
                deathCount = firstDeath.get("count");
            }

            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            String correlationId = extractCorrelationId(body);
            if (body.length() > MAX_PAYLOAD_CHARS) {
                body = body.substring(0, MAX_PAYLOAD_CHARS) + "...(truncado)";
            }

            log.warn("[DLQ] Mensaje NO ENTREGADO | dlq={} | colaOrigen={} | motivo={} | veces={} | correlationId={} | payload={}",
                    props.getConsumerQueue(), sourceQueue, reason, deathCount, correlationId, body);
        } catch (Exception e) {
            // Registrar nunca debe impedir confirmar: un mensaje en la DLQ no se reencola.
            log.error("[DLQ] No se pudo registrar el detalle del mensaje no entregado", e);
        }
        channel.basicAck(tag, false);
    }

    private String extractCorrelationId(String body) {
        try {
            JsonNode envelope = objectMapper.readTree(body);
            return envelope == null ? "-" : envelope.path("correlationId").asText("-");
        } catch (Exception e) {
            return "-";   // el cuerpo no era JSON: justo el caso típico de un mensaje no recuperable
        }
    }
}