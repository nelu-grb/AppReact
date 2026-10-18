package com.react.backend.msnofity.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    @RabbitListener(queues = {
            "${app.rabbitmq.queue.housekeeping.dlq}",
            "${app.rabbitmq.queue.voucher.dlq}",
            "${app.rabbitmq.queue.email.dlq}"
    })
    public void logDeadLetter(Message message, Channel channel) throws IOException {
        String queue = message.getMessageProperties().getConsumerQueue();
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        log.error("Mensaje en DLQ: cola={}, routingKey={}, x-death={}, payload={}",
                queue,
                message.getMessageProperties().getReceivedRoutingKey(),
                message.getMessageProperties().getHeaders().get("x-death"),
                payload);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
