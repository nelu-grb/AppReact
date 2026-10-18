package com.andesstay.msreservations.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RabbitMQConfigTest {

    @Test
    void declaresBoundedQueuesWithExpirationAndDeadLetterRouting() {
        try (AnnotationConfigApplicationContext context = createContext()) {
            assertQueue(context.getBean("housekeepingQueue", Queue.class),
                    "q.housekeeper.dlq", true);
            assertQueue(context.getBean("voucherQueue", Queue.class),
                    "q.voucher.dlq", true);
            assertQueue(context.getBean("emailQueue", Queue.class),
                    "q.email.dlq", true);
            assertQueue(context.getBean("housekeepingDeadLetterQueue", Queue.class),
                    null, false);
            assertQueue(context.getBean("voucherDeadLetterQueue", Queue.class),
                    null, false);
            assertQueue(context.getBean("emailDeadLetterQueue", Queue.class),
                    null, false);
        }
    }

    private AnnotationConfigApplicationContext createContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                "rabbit-test",
                Map.ofEntries(
                        Map.entry("app.rabbitmq.exchange.direct", "cmd.direct"),
                        Map.entry("app.rabbitmq.exchange.topic", "evt.topic"),
                        Map.entry("app.rabbitmq.exchange.dlx", "cmd.dead.dlx"),
                        Map.entry("app.rabbitmq.queue.housekeeping", "q.housekeeper"),
                        Map.entry("app.rabbitmq.queue.voucher", "q.voucher"),
                        Map.entry("app.rabbitmq.queue.email", "q.email"),
                        Map.entry("app.rabbitmq.queue.housekeeping.dlq", "q.housekeeper.dlq"),
                        Map.entry("app.rabbitmq.queue.voucher.dlq", "q.voucher.dlq"),
                        Map.entry("app.rabbitmq.queue.email.dlq", "q.email.dlq"),
                        Map.entry("app.rabbitmq.routing.commands.housekeeping", "housekeeping.ticket"),
                        Map.entry("app.rabbitmq.routing.commands.voucher", "voucher.gen"),
                        Map.entry("app.rabbitmq.routing.notifications.binding", "notification.#"),
                        Map.entry("app.rabbitmq.queue.max-length", "10000"),
                        Map.entry("app.rabbitmq.queue.message-ttl-ms", "86400000"),
                        Map.entry("app.rabbitmq.queue.overflow", "reject-publish-dlx"),
                        Map.entry("app.rabbitmq.queue.dlq.max-length", "10000"),
                        Map.entry("app.rabbitmq.queue.dlq.message-ttl-ms", "2592000000")
                )));
        context.register(RabbitMQConfig.class);
        context.refresh();
        return context;
    }

    private void assertQueue(Queue queue, String deadLetterQueue, boolean workQueue) {
        Map<String, Object> arguments = queue.getArguments();
        if (workQueue) {
            assertEquals(10000, arguments.get("x-max-length"));
            assertEquals(86400000L, arguments.get("x-message-ttl"));
            assertEquals("reject-publish-dlx", arguments.get("x-overflow"));
            assertEquals("cmd.dead.dlx", arguments.get("x-dead-letter-exchange"));
            assertEquals(deadLetterQueue, arguments.get("x-dead-letter-routing-key"));
        } else {
            assertEquals(10000, arguments.get("x-max-length"));
            assertEquals(2592000000L, arguments.get("x-message-ttl"));
        }
    }
}
