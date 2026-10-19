package com.react.backend.msnofity.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.react.backend.msnofity.config.RabbitMQConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
class RabbitMQTopologyIntegrationTest {

    private static final String HOUSEKEEPING = "q.housekeeper";
    private static final String VOUCHER = "q.voucher";
    private static final String EMAIL = "q.email";
    private static final String HOUSEKEEPING_DLQ = "q.housekeeper.dlq";
    private static final String VOUCHER_DLQ = "q.voucher.dlq";
    private static final String EMAIL_DLQ = "q.email.dlq";

    @Container
    private static final RabbitMQContainer rabbit = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.13-management"));

    private AnnotationConfigApplicationContext context;
    private RabbitTemplate rabbitTemplate;
    private CachingConnectionFactory connectionFactory;

    @BeforeEach
    void declareApplicationTopology() {
        connectionFactory = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
        connectionFactory.setUsername(rabbit.getAdminUsername());
        connectionFactory.setPassword(rabbit.getAdminPassword());

        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("rabbit-test", Map.ofEntries(
                Map.entry("app.rabbitmq.exchange.direct", "cmd.direct"),
                Map.entry("app.rabbitmq.exchange.topic", "evt.topic"),
                Map.entry("app.rabbitmq.exchange.dlx", "cmd.dead.dlx"),
                Map.entry("app.rabbitmq.queue.housekeeping", HOUSEKEEPING),
                Map.entry("app.rabbitmq.queue.voucher", VOUCHER),
                Map.entry("app.rabbitmq.queue.email", EMAIL),
                Map.entry("app.rabbitmq.queue.housekeeping.dlq", HOUSEKEEPING_DLQ),
                Map.entry("app.rabbitmq.queue.voucher.dlq", VOUCHER_DLQ),
                Map.entry("app.rabbitmq.queue.email.dlq", EMAIL_DLQ),
                Map.entry("app.rabbitmq.routing.commands.housekeeping", "housekeeping.ticket"),
                Map.entry("app.rabbitmq.routing.commands.voucher", "voucher.gen"),
                Map.entry("app.rabbitmq.routing.notifications.binding", "notification.#"),
                Map.entry("app.rabbitmq.routing.notifications.email", "notification.email")
        )));
        context.registerBean(CachingConnectionFactory.class, () -> connectionFactory);
        context.registerBean(RabbitAdmin.class, () -> new RabbitAdmin(connectionFactory));
        context.register(RabbitMQConfig.class);
        context.refresh();
        rabbitTemplate = new RabbitTemplate(connectionFactory);
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void routesDirectAndTopicMessagesToTheirBoundQueues() {
        rabbitTemplate.convertAndSend("cmd.direct", "housekeeping.ticket", "housekeeping");
        rabbitTemplate.convertAndSend("cmd.direct", "voucher.gen", "voucher");
        rabbitTemplate.convertAndSend("evt.topic", "notification.email", "notification");

        assertEquals("housekeeping", receiveBody(HOUSEKEEPING));
        assertEquals("voucher", receiveBody(VOUCHER));
        assertEquals("notification", receiveBody(EMAIL));
    }

    @Test
    void rejectsEachDomainWithoutRequeueAndRoutesToItsDeadLetterQueue() throws Exception {
        rejectToDeadLetterQueue("cmd.direct", "housekeeping.ticket", HOUSEKEEPING, HOUSEKEEPING_DLQ);
        rejectToDeadLetterQueue("cmd.direct", "voucher.gen", VOUCHER, VOUCHER_DLQ);
        rejectToDeadLetterQueue("evt.topic", "notification.email", EMAIL, EMAIL_DLQ);

        assertEquals("invalid-housekeeping", receiveBody(HOUSEKEEPING_DLQ));
        assertEquals("invalid-voucher", receiveBody(VOUCHER_DLQ));
        assertEquals("invalid-email", receiveBody(EMAIL_DLQ));
    }

    private void rejectToDeadLetterQueue(String exchange, String routingKey, String sourceQueue, String deadLetterQueue)
            throws IOException, TimeoutException {
        String body = "invalid-" + sourceQueue.substring(sourceQueue.lastIndexOf('.') + 1);
        rabbitTemplate.convertAndSend(exchange, routingKey, body);
        try (Connection connection = connectionFactory.getRabbitConnectionFactory().newConnection();
             Channel channel = connection.createChannel()) {
            com.rabbitmq.client.GetResponse response = channel.basicGet(sourceQueue, false);
            assertNotNull(response, "El mensaje debe permanecer pendiente en " + sourceQueue);
            channel.basicNack(response.getEnvelope().getDeliveryTag(), false, false);
        }
    }

    private String receiveBody(String queue) {
        Message message = rabbitTemplate.receive(queue, 5000);
        assertNotNull(message, "No se recibió el mensaje esperado desde " + queue);
        return new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
    }

}
