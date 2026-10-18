package com.react.backend.msnofity.listener;

import com.react.backend.msnofity.config.RabbitMQConfig;
import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import com.react.backend.msnofity.dto.NotificationEvent;
import com.react.backend.msnofity.dto.VoucherEvent;
import com.react.backend.msnofity.service.HousekeepingService;
import com.react.backend.msnofity.service.NotificationService;
import com.react.backend.msnofity.service.VoucherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;
    private final HousekeepingService housekeepingService;
    private final VoucherService voucherService;
    private final ObjectMapper objectMapper;

    public NotificationListener(
            NotificationService notificationService,
            HousekeepingService housekeepingService,
            VoucherService voucherService,
            ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.housekeepingService = housekeepingService;
        this.voucherService = voucherService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.email}")
    public void processEmail(Message message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            log.info("Mensaje recibido de q.cmd.email: {}", envelope);
            JsonNode payload = envelope.path("payload");
            NotificationEvent event = new NotificationEvent(
                    envelope.path("type").asText("EMAIL_SEND").replace("_SEND", ""),
                    payload.path("email").asText(),
                    payload.path("subject").asText(),
                    payload.path("body").asText());
            if ("EMAIL".equalsIgnoreCase(event.type())) {
                notificationService.sendEmail(event);
            } else if ("WEBPUSH".equalsIgnoreCase(event.type())) {
                notificationService.sendWebPush(event);
            } else {
                log.warn("Tipo de notificación no soportado: {}", event.type());
            }
            // Ack manual: el mensaje fue procesado correctamente
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error procesando email en msnofity", e);
            // Nack manual con requeue=false: envía el mensaje a la DLQ
            channel.basicNack(tag, false, false);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.housekeeping}")
    public void processHousekeeping(Message message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            JsonNode payload = envelope.path("payload");
            housekeepingService.createTicket(new HousekeepingTicketEvent(
                    envelope.path("correlationId").asText(),
                    payload.path("unitId").asText(),
                    payload.path("detail").asText(),
                    "NORMAL"));
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error procesando housekeeping ticket en msnofity", e);
            channel.basicNack(tag, false, false);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.voucher}")
    public void processVoucher(Message message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            JsonNode payload = envelope.path("payload");
            voucherService.generateVoucher(new VoucherEvent(
                    payload.path("reservationId").asText(),
                    payload.path("customerEmail").asText(),
                    payload.path("voucherCode").asText(),
                    payload.path("amount").asDouble(0D)
            ));
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error procesando voucher en msnofity", e);
            channel.basicNack(tag, false, false);
        }
    }

    // Listener para la cola durable del Fanout (Pub/Sub)
    @RabbitListener(queues = RabbitMQConfig.EVT_NOTIFY_QUEUE)
    public void onEventNotify(Message message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            log.info("[NOTIFY] Evento recibido: {}", envelope.path("type").asText());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error en evento notify", e);
            channel.basicNack(tag, false, false);
        }
    }

    // Listener para la cola temporal de monitoreo del Fanout
    @RabbitListener(queues = "#{evtMonitorQueue.name}")
    public void onEventMonitor(Message message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            log.info("[MONITOR] Evento monitoreado: {}", envelope);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error en evento monitor", e);
            channel.basicNack(tag, false, false);
        }
    }
}