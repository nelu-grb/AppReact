package com.andesstay.msreservations.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitMQPublisherTest {

    private RabbitTemplate rabbitTemplate;
    private RabbitMQPublisher publisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        publisher = new RabbitMQPublisher(rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "directExchange", "cmd.direct");
        ReflectionTestUtils.setField(publisher, "topicExchange", "evt.topic");
        ReflectionTestUtils.setField(publisher, "housekeepingRoutingKey", "housekeeping.ticket");
        ReflectionTestUtils.setField(publisher, "voucherRoutingKey", "voucher.gen");
        ReflectionTestUtils.setField(publisher, "notificationEmailRoutingKey", "notification.email");
    }

    @Test
    void publishesEachCommandAndNotificationToConfiguredRoutes() {
        publisher.publishHousekeepingTicket(42L, "Prepare unit", "RES-100");
        publisher.publishVoucherGenCommand(100L, "guest@example.com", BigDecimal.TEN, "RES-100");
        publisher.publishEmailCommand("guest@example.com", "Confirmed", "Your reservation is confirmed", "RES-100");

        verify(rabbitTemplate).convertAndSend(
                eq("cmd.direct"),
                eq("housekeeping.ticket"),
                any(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class));
        verify(rabbitTemplate).convertAndSend(
                eq("cmd.direct"),
                eq("voucher.gen"),
                any(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class));
        verify(rabbitTemplate).convertAndSend(
                eq("evt.topic"),
                eq("notification.email"),
                any(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class));
    }
}
