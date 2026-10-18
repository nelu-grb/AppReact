package com.react.backend.msnofity.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.react.backend.msnofity.dto.VoucherEvent;
import com.react.backend.msnofity.messaging.ManualAckMessageProcessor;
import com.react.backend.msnofity.service.VoucherService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VoucherListener {

    private final VoucherService voucherService;
    private final ObjectMapper objectMapper;
    private final ManualAckMessageProcessor messageProcessor;

    public VoucherListener(
            VoucherService voucherService,
            ObjectMapper objectMapper,
            ManualAckMessageProcessor messageProcessor) {
        this.voucherService = voucherService;
        this.objectMapper = objectMapper;
        this.messageProcessor = messageProcessor;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.voucher}")
    public void processVoucher(Message message, Channel channel) throws IOException {
        messageProcessor.process("voucher", channel, message.getMessageProperties().getDeliveryTag(), () -> {
            JsonNode envelope = objectMapper.readTree(message.getBody());
            JsonNode payload = envelope.path("payload");
            voucherService.generateVoucher(new VoucherEvent(
                    payload.path("reservationId").asText(),
                    payload.path("customerEmail").asText(),
                    payload.path("voucherCode").asText(),
                    payload.path("amount").asDouble(0D)));
            return null;
        });
    }
}
