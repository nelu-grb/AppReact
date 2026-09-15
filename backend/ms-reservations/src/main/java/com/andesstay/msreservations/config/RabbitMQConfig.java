package com.andesstay.msreservations.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // Queues
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_HOUSEKEEPING = "q.cmd.housekeeping";
    public static final String QUEUE_VOUCHER = "q.cmd.voucher";
    public static final String QUEUE_EMAIL_DLQ = QUEUE_EMAIL + ".dlq";
    public static final String QUEUE_HOUSEKEEPING_DLQ = QUEUE_HOUSEKEEPING + ".dlq";
    public static final String QUEUE_VOUCHER_DLQ = QUEUE_VOUCHER + ".dlq";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_EMAIL + ".dlq")
                .build();
    }

    @Bean
    public Queue housekeepingQueue() {
        return QueueBuilder.durable(QUEUE_HOUSEKEEPING)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_HOUSEKEEPING + ".dlq")
                .build();
    }

    @Bean
    public Queue voucherQueue() {
        return QueueBuilder.durable(QUEUE_VOUCHER)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_VOUCHER + ".dlq")
                .build();
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL_DLQ).build();
    }

    @Bean
    public Queue housekeepingDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_HOUSEKEEPING_DLQ).build();
    }

    @Bean
    public Queue voucherDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_VOUCHER_DLQ).build();
    }

    @Bean
    public Binding bindingEmailDirect(@Qualifier("emailQueue") Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with("email.send");
    }

    @Bean
    public Binding bindingHousekeepingDirect(@Qualifier("housekeepingQueue") Queue housekeepingQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(directExchange).with("housekeeping.ticket");
    }

    @Bean
    public Binding bindingVoucherDirect(@Qualifier("voucherQueue") Queue voucherQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(voucherQueue).to(directExchange).with("voucher.gen");
    }

    @Bean
    public Binding bindingEmailDeadLetter(@Qualifier("emailDeadLetterQueue") Queue emailDeadLetterQueue,
                                         @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue).to(deadLetterExchange).with(QUEUE_EMAIL_DLQ);
    }

    @Bean
    public Binding bindingHousekeepingDeadLetter(@Qualifier("housekeepingDeadLetterQueue") Queue housekeepingDeadLetterQueue,
                                                 @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(housekeepingDeadLetterQueue).to(deadLetterExchange).with(QUEUE_HOUSEKEEPING_DLQ);
    }

    @Bean
    public Binding bindingVoucherDeadLetter(@Qualifier("voucherDeadLetterQueue") Queue voucherDeadLetterQueue,
                                            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(voucherDeadLetterQueue).to(deadLetterExchange).with(QUEUE_VOUCHER_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}