package com.react.backend.msnofity.listener;

import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.messaging.ReliableMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio: eventos de reserva publicados por ms-reservations en el exchange topic.
 *
 * Dos colas, dos patrones de binding sobre el mismo exchange:
 *   - q.evt.notify (durable)  <- reservation.*  (solo eventos de reserva)
 *   - cola anónima (temporal) <- #              (todos los eventos, para monitoreo)
 */
@Component
public class ReservationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventListener.class);

    private final ReliableMessageHandler handler;

    public ReservationEventListener(ReliableMessageHandler handler) {
        this.handler = handler;
    }

    /** Cola durable del exchange topic. */
    @RabbitListener(queues = "${app.rabbitmq.queue.evt-notify}")
    public void onEventNotify(Message message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        handler.handle("evt-notify", message, channel, tag, envelope ->
                log.info("[NOTIFY] Evento recibido: {} | routingKey={}",
                        envelope.path("type").asText(),
                        message.getMessageProperties().getReceivedRoutingKey()));
    }

    /** Cola temporal de monitoreo. */
    @RabbitListener(queues = "#{evtMonitorQueue.name}")
    public void onEventMonitor(Message message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        handler.handle("evt-monitor", message, channel, tag, envelope ->
                log.info("[MONITOR] Evento monitoreado: {} | routingKey={}",
                        envelope,
                        message.getMessageProperties().getReceivedRoutingKey()));
    }
}