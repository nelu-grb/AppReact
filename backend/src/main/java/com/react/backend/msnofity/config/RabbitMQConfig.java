package com.react.backend.msnofity.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${app.rabbitmq.queue.housekeeping}")
    private String housekeepingQueue;

    @Value("${app.rabbitmq.queue.voucher}")
    private String voucherQueue;

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue).build();
    }

    @Bean
    public Queue housekeepingQueue() {
        return QueueBuilder.durable(housekeepingQueue).build();
    }

    @Bean
    public Queue voucherQueue() {
        return QueueBuilder.durable(voucherQueue).build();
    }

    // Indispensable para convertir automáticamente el JSON enviado por msreservation a Objetos Java (DTOs)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}