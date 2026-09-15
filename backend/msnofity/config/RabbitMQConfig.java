package com.react.backend.msnofity.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dlx";

    @Value("${app.rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${app.rabbitmq.queue.email-dlq}")
    private String emailDlq;

    @Value("${app.rabbitmq.queue.housekeeping}")
    private String housekeepingQueue;

    @Value("${app.rabbitmq.queue.housekeeping-dlq}")
    private String housekeepingDlq;

    @Value("${app.rabbitmq.queue.voucher}")
    private String voucherQueue;

    @Value("${app.rabbitmq.queue.voucher-dlq}")
    private String voucherDlq;

    // ===== Exchanges =====
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    // ===== Cola email =====
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "q.cmd.email.dlq")
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(emailDlq).build();
    }

    @Bean
    public Binding bindingEmailDlq(Queue emailDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(emailDlq).to(dlxExchange).with("q.cmd.email.dlq");
    }

    @Bean
    public Binding bindingEmailDirect(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with("email.send");
    }

    @Bean
    public Binding bindingEmailTopic(Queue emailQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(emailQueue).to(topicExchange).with("email.*");
    }

    // ===== Cola housekeeping =====
    @Bean
    public Queue housekeepingQueue() {
        return QueueBuilder.durable(housekeepingQueue)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "housekeeping.ticket.dlq")
                .build();
    }

    @Bean
    public Queue housekeepingDlq() {
        return QueueBuilder.durable(housekeepingDlq).build();
    }

    @Bean
    public Binding bindingHousekeepingDlq(Queue housekeepingDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(housekeepingDlq).to(dlxExchange).with("housekeeping.ticket.dlq");
    }

    @Bean
    public Binding bindingHousekeepingDirect(Queue housekeepingQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(directExchange).with("housekeeping.ticket");
    }

    @Bean
    public Binding bindingHousekeepingTopic(Queue housekeepingQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(topicExchange).with("housekeeping.#");
    }

    // ===== Cola voucher =====
    @Bean
    public Queue voucherQueue() {
        return QueueBuilder.durable(voucherQueue)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "q.cmd.voucher.dlq")
                .build();
    }

    @Bean
    public Queue voucherDlq() {
        return QueueBuilder.durable(voucherDlq).build();
    }

    @Bean
    public Binding bindingVoucherDlq(Queue voucherDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(voucherDlq).to(dlxExchange).with("q.cmd.voucher.dlq");
    }

    @Bean
    public Binding bindingVoucherDirect(Queue voucherQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(voucherQueue).to(directExchange).with("voucher.gen");
    }

    @Bean
    public Binding bindingVoucherTopic(Queue voucherQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(voucherQueue).to(topicExchange).with("voucher.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JsonMessageConverter.TypePrecedence.INFERRED);
        return converter;
    }
}