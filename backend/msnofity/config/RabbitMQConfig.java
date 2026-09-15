package com.react.backend.msnofity.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    @Value("${app.rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${app.rabbitmq.queue.housekeeping}")
    private String housekeepingQueue;

    @Value("${app.rabbitmq.queue.voucher}")
    private String voucherQueue;

    private String emailDeadLetterQueueName() {
        return emailQueue + ".dlq";
    }

    private String housekeepingDeadLetterQueueName() {
        return housekeepingQueue + ".dlq";
    }

    private String voucherDeadLetterQueueName() {
        return voucherQueue + ".dlq";
    }

    // Direct Exchange único
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    // Definición de Colas
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue)
            .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", emailDeadLetterQueueName())
            .build();
    }

    @Bean
    public Queue housekeepingQueue() {
        return QueueBuilder.durable(housekeepingQueue)
            .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", housekeepingDeadLetterQueueName())
            .build();
    }

    @Bean
    public Queue voucherQueue() {
        return QueueBuilder.durable(voucherQueue)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", voucherDeadLetterQueueName())
                .build();
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(emailDeadLetterQueueName()).build();
    }

    @Bean
    public Queue housekeepingDeadLetterQueue() {
        return QueueBuilder.durable(housekeepingDeadLetterQueueName()).build();
    }

    @Bean
    public Queue voucherDeadLetterQueue() {
        return QueueBuilder.durable(voucherDeadLetterQueueName()).build();
    }

    // Bindings
    @Bean
    public Binding bindingEmail(@Qualifier("emailQueue") Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with("email.send");
    }

    @Bean
    public Binding bindingHousekeeping(@Qualifier("housekeepingQueue") Queue housekeepingQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(directExchange).with("housekeeping.ticket");
    }

    @Bean
    public Binding bindingVoucher(@Qualifier("voucherQueue") Queue voucherQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(voucherQueue).to(directExchange).with("voucher.gen");
    }

    @Bean
    public Binding bindingEmailDeadLetter(@Qualifier("emailDeadLetterQueue") Queue emailDeadLetterQueue,
                                          @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue).to(deadLetterExchange).with(emailDeadLetterQueueName());
    }

    @Bean
    public Binding bindingHousekeepingDeadLetter(@Qualifier("housekeepingDeadLetterQueue") Queue housekeepingDeadLetterQueue,
                                                 @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(housekeepingDeadLetterQueue).to(deadLetterExchange).with(housekeepingDeadLetterQueueName());
    }

    @Bean
    public Binding bindingVoucherDeadLetter(@Qualifier("voucherDeadLetterQueue") Queue voucherDeadLetterQueue,
                                            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(voucherDeadLetterQueue).to(deadLetterExchange).with(voucherDeadLetterQueueName());
    }

    // Convertidor de mensajes a JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        Jackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}