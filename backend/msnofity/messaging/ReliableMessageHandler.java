package com.react.backend.msnofity.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.exception.NonRecoverableMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

/**
 * Lógica de mensajería común a todos los consumidores de comandos.
 * Los listeners solo dicen QUÉ hacer con el mensaje; esta clase decide
 * CÓMO confirmarlo:
 *
 *   1. Cuerpo que no es un JSON objeto ......... error no recuperable -> NACK sin requeue (DLQ)
 *   2. Error no recuperable al procesar ........ NACK sin requeue (DLQ), sin reintentos
 *   3. Error recuperable (red, 5xx, 429...) .... reintento acotado con espera creciente;
 *                                                 si se agotan los intentos -> NACK sin requeue (DLQ)
 *   4. Procesamiento correcto .................. ACK
 *
 * Los reintentos se hacen aquí y no con spring.rabbitmq.listener.simple.retry.*
 * porque con acknowledge-mode=manual el listener captura las excepciones y el
 * interceptor de reintentos de Spring nunca se enteraría.
 */
@Component
public class ReliableMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ReliableMessageHandler.class);

    /** Lo que cada listener implementa: procesar el sobre JSON del mensaje. */
    @FunctionalInterface
    public interface EnvelopeProcessor {
        void process(JsonNode envelope) throws Exception;
    }

    private final ObjectMapper objectMapper;
    private final int maxAttempts;
    private final long initialIntervalMs;
    private final double multiplier;
    private final long maxIntervalMs;

    public ReliableMessageHandler(
            ObjectMapper objectMapper,
            @Value("${app.rabbitmq.retry.max-attempts:3}") int maxAttempts,
            @Value("${app.rabbitmq.retry.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${app.rabbitmq.retry.multiplier:2.0}") double multiplier,
            @Value("${app.rabbitmq.retry.max-interval-ms:10000}") long maxIntervalMs) {
        this.objectMapper = objectMapper;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialIntervalMs = Math.max(0, initialIntervalMs);
        this.multiplier = Math.max(1.0, multiplier);
        this.maxIntervalMs = Math.max(this.initialIntervalMs, maxIntervalMs);
    }

    /**
     * @param label     nombre corto de la cola/dominio, solo para los logs
     * @param processor lógica del listener
     */
    public void handle(String label, Message message, Channel channel, long tag,
                       EnvelopeProcessor processor) throws IOException {

        // 1) El cuerpo debe ser un JSON objeto; si no, reintentar no sirve de nada.
        JsonNode envelope;
        try {
            envelope = objectMapper.readTree(message.getBody());
        } catch (Exception e) {
            log.error("[{}] Error NO recuperable: el cuerpo no es JSON válido ({}). NACK sin requeue -> DLQ",
                    label, describe(e));
            channel.basicNack(tag, false, false);
            return;
        }
        if (envelope == null || !envelope.isObject()) {
            log.error("[{}] Error NO recuperable: el cuerpo no es un objeto JSON. NACK sin requeue -> DLQ", label);
            channel.basicNack(tag, false, false);
            return;
        }

        String correlationId = envelope.path("correlationId").asText("-");
        log.info("[{}] Mensaje recibido | correlationId={} | type={}",
                label, correlationId, envelope.path("type").asText("-"));

        // 2) Procesar con reintentos acotados.
        Exception failure = null;
        long delay = initialIntervalMs;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                processor.process(envelope);
                failure = null;
                if (attempt > 1) {
                    log.info("[{}] Procesado correctamente en el intento {}/{} | correlationId={}",
                            label, attempt, maxAttempts, correlationId);
                }
                break;
            } catch (InterruptedException e) {
                // La aplicación se está apagando: devolver el mensaje a la cola, no mandarlo a la DLQ.
                Thread.currentThread().interrupt();
                log.warn("[{}] Procesamiento interrumpido. NACK con requeue | correlationId={}",
                        label, correlationId);
                channel.basicNack(tag, false, true);
                return;
            } catch (Exception e) {
                failure = e;

                if (!isRecoverable(e)) {
                    log.error("[{}] Error NO recuperable | correlationId={} | {}",
                            label, correlationId, describe(e), e);
                    break;
                }

                if (attempt < maxAttempts) {
                    log.warn("[{}] Error recuperable en intento {}/{} | correlationId={} | {} | reintentando en {} ms",
                            label, attempt, maxAttempts, correlationId, describe(e), delay);
                    if (!pause(delay)) {
                        channel.basicNack(tag, false, true);
                        return;
                    }
                    delay = Math.min((long) (delay * multiplier), maxIntervalMs);
                } else {
                    log.error("[{}] Error recuperable: se agotaron los {} intentos | correlationId={} | {}",
                            label, maxAttempts, correlationId, describe(e), e);
                }
            }
        }

        // 3) Confirmar según el resultado.
        if (failure == null) {
            channel.basicAck(tag, false);
        } else {
            log.error("[{}] NACK sin requeue -> DLQ | correlationId={}", label, correlationId);
            channel.basicNack(tag, false, false);
        }
    }

    /**
     * Recuperable = vale la pena reintentar (fallo transitorio).
     * No recuperable = el mismo mensaje fallará siempre.
     */
    static boolean isRecoverable(Throwable error) {
        if (error instanceof NonRecoverableMessageException) {
            return false;
        }
        if (error instanceof HttpClientErrorException.TooManyRequests) {
            return true;                       // 429: el servicio pide esperar
        }
        if (error instanceof HttpClientErrorException) {
            return false;                      // resto de 4xx: la petición es inválida
        }
        if (error instanceof IllegalArgumentException) {
            return false;                      // dato inválido
        }
        return true;                           // red caída, 5xx, base de datos, etc.
    }

    private boolean pause(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static String describe(Throwable e) {
        String msg = e.getMessage();
        if (msg != null) {
            int newLine = msg.indexOf('\n');
            if (newLine >= 0) {
                msg = msg.substring(0, newLine);
            }
        }
        return e.getClass().getSimpleName() + (msg == null ? "" : ": " + msg);
    }
}