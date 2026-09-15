package com.react.backend.msnofity.listener;

import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import com.react.backend.msnofity.dto.NotificationEvent;
import com.react.backend.msnofity.dto.VoucherEvent;
import com.react.backend.msnofity.service.HousekeepingService;
import com.react.backend.msnofity.service.NotificationService;
import com.react.backend.msnofity.service.VoucherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

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
    public void processEmail(JsonNode envelope) {
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
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.housekeeping}")
    public void processHousekeeping(JsonNode envelope) {
        log.info("Mensaje recibido de q.cmd.housekeeping: {}", envelope);

        JsonNode payload = envelope.path("payload");

        String reservationId = envelope.path("correlationId").asText(null);
        String unitId = payload.path("unitId").asText(null);
        String detail = payload.path("detail").asText(null);

        if (reservationId == null || unitId == null || detail == null) {
            log.error("Mensaje de housekeeping malformado, faltan campos obligatorios: {}", envelope);
            throw new IllegalArgumentException("Payload de housekeeping inválido: correlationId/unitId/detail requeridos");
        }

        housekeepingService.createTicket(new HousekeepingTicketEvent(
                reservationId,
                unitId,
                detail,
                "NORMAL"));
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.voucher}")
    public void processVoucher(JsonNode envelope) {
        JsonNode payload = envelope.path("payload");
        voucherService.generateVoucher(new VoucherEvent(
                payload.path("reservationId").asText(), "", "", 0D));
    }
}