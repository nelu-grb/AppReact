package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.dto.VoucherEvent;
import com.react.backend.msnofity.messaging.ReliableMessageHandler;
import com.react.backend.msnofity.service.VoucherService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio: vouchers (cola de comandos q.cmd.voucher).
 */
@Component
public class VoucherListener {

    private final VoucherService voucherService;
    private final ReliableMessageHandler handler;

    public VoucherListener(VoucherService voucherService, ReliableMessageHandler handler) {
        this.voucherService = voucherService;
        this.handler = handler;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.voucher}")
    public void processVoucher(Message message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        handler.handle("voucher", message, channel, tag, envelope -> {
            JsonNode payload = envelope.path("payload");
            voucherService.generateVoucher(new VoucherEvent(
                    payload.path("reservationId").asText(),
                    payload.path("customerEmail").asText(),
                    payload.path("voucherCode").asText(),
                    payload.path("amount").asDouble(0D)));
        });
    }
}