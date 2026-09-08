package com.react.backend.msnofity.listener;

import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import com.react.backend.msnofity.dto.NotificationEvent;
import com.react.backend.msnofity.dto.VoucherEvent;
import com.react.backend.msnofity.service.HousekeepingService;
import com.react.backend.msnofity.service.NotificationService;
import com.react.backend.msnofity.service.VoucherService;
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

    public NotificationListener(
            NotificationService notificationService,
            HousekeepingService housekeepingService,
            VoucherService voucherService) {
        this.notificationService = notificationService;
        this.housekeepingService = housekeepingService;
        this.voucherService = voucherService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.email}")
    public void processEmail(NotificationEvent event) {
        log.info("Mensaje recibido de q.cmd.email: {}", event);
        if ("EMAIL".equalsIgnoreCase(event.type())) {
            notificationService.sendEmail(event);
        } else if ("WEBPUSH".equalsIgnoreCase(event.type())) {
            notificationService.sendWebPush(event);
        } else {
            log.warn("Tipo de notificación no soportado: {}", event.type());
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.housekeeping}")
    public void processHousekeeping(HousekeepingTicketEvent event) {
        log.info("Mensaje recibido de q.cmd.housekeeping: {}", event);
        housekeepingService.createTicket(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.voucher}")
    public void processVoucher(VoucherEvent event) {
        log.info("Mensaje recibido de q.cmd.voucher: {}", event);
        voucherService.generateVoucher(event);
    }
}