package com.react.backend.msnofity.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

@Component
public class ManualAckMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ManualAckMessageProcessor.class);

    private final int maxAttempts;
    private final long initialIntervalMs;
    private final long maxIntervalMs;

    public ManualAckMessageProcessor(
            @Value("${app.rabbitmq.listener.retry.max-attempts}") int maxAttempts,
            @Value("${app.rabbitmq.listener.retry.initial-interval-ms}") long initialIntervalMs,
            @Value("${app.rabbitmq.listener.retry.max-interval-ms}") long maxIntervalMs) {
        if (maxAttempts < 1 || initialIntervalMs < 0 || maxIntervalMs < initialIntervalMs) {
            throw new IllegalArgumentException("Configuración de reintentos RabbitMQ inválida");
        }
        this.maxAttempts = maxAttempts;
        this.initialIntervalMs = initialIntervalMs;
        this.maxIntervalMs = maxIntervalMs;
    }

    public void process(String domain, Channel channel, long deliveryTag, Callable<Void> action)
            throws IOException {
        try {
            executeWithRetry(domain, action);
        } catch (Exception exception) {
            if (isRecoverable(exception)) {
                log.error("Se agotaron los {} intentos para {}. Se enviará a la DLQ",
                        maxAttempts, domain, exception);
            } else {
                log.error("Error no recuperable en {}. Se enviará a la DLQ",
                        domain, exception);
            }
            channel.basicNack(deliveryTag, false, false);
            return;
        }
        channel.basicAck(deliveryTag, false);
    }

    private void executeWithRetry(String domain, Callable<Void> action) throws Exception {
        long delayMs = initialIntervalMs;
        for (int attempt = 1; ; attempt++) {
            try {
                action.call();
                return;
            } catch (Exception exception) {
                if (!isRecoverable(exception) || attempt >= maxAttempts) {
                    throw exception;
                }
                log.warn("Error recuperable en {} (intento {}/{}); se reintentará",
                        domain, attempt, maxAttempts, exception);
                sleepBeforeRetry(delayMs);
                delayMs = Math.min(maxIntervalMs, delayMs > maxIntervalMs / 2
                        ? maxIntervalMs
                        : delayMs * 2);
            }
        }
    }

    private boolean isRecoverable(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof JsonProcessingException) {
                return false;
            }
            if (cause instanceof ResourceAccessException
                    || cause instanceof AmqpConnectException
                    || cause instanceof AmqpIOException
                    || cause instanceof SocketTimeoutException
                    || cause instanceof ConnectException
                    || cause instanceof IOException) {
                return true;
            }
            if (cause instanceof RestClientResponseException responseException
                    && (responseException.getStatusCode().is5xxServerError()
                    || responseException.getStatusCode().value() == 429)) {
                return true;
            }
            if (cause == cause.getCause()) {
                break;
            }
        }
        return false;
    }

    private void sleepBeforeRetry(long delayMs) throws InterruptedException {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        }
    }
}
