package com.react.backend.msnofity.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.direct}")
    private String directExchangeName;

    @Value("${app.rabbitmq.exchange.topic}")
    private String topicExchangeName;

    @Value("${app.rabbitmq.exchange.dlx}")
    private String deadLetterExchangeName;

    @Value("${app.rabbitmq.queue.housekeeping}")
    private String housekeepingQueueName;

    @Value("${app.rabbitmq.queue.voucher}")
    private String voucherQueueName;

    @Value("${app.rabbitmq.queue.email}")
    private String emailQueueName;

    @Value("${app.rabbitmq.queue.housekeeping.dlq}")
    private String housekeepingDeadLetterQueueName;

    @Value("${app.rabbitmq.queue.voucher.dlq}")
    private String voucherDeadLetterQueueName;

    @Value("${app.rabbitmq.queue.email.dlq}")
    private String emailDeadLetterQueueName;

    @Value("${app.rabbitmq.routing.commands.housekeeping}")
    private String housekeepingRoutingKey;

    @Value("${app.rabbitmq.routing.commands.voucher}")
    private String voucherRoutingKey;

    @Value("${app.rabbitmq.routing.notifications.binding}")
    private String notificationsBindingKey;

    @Value("${app.rabbitmq.queue.max-length:10000}")
    private int queueMaxLength;

    @Value("${app.rabbitmq.queue.message-ttl-ms:86400000}")
    private long queueMessageTtlMs;

    @Value("${app.rabbitmq.queue.overflow:reject-publish-dlx}")
    private String queueOverflow;

    @Value("${app.rabbitmq.queue.dlq.max-length:10000}")
    private int deadLetterQueueMaxLength;

    @Value("${app.rabbitmq.queue.dlq.message-ttl-ms:2592000000}")
    private long deadLetterQueueMessageTtlMs;

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchangeName, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName, true, false);
    }

    @Bean
    public Queue housekeepingQueue() {
        return durableWorkQueue(housekeepingQueueName, housekeepingDeadLetterQueueName);
    }

    @Bean
    public Queue voucherQueue() {
        return durableWorkQueue(voucherQueueName, voucherDeadLetterQueueName);
    }

    @Bean
    public Queue emailQueue() {
        return durableWorkQueue(emailQueueName, emailDeadLetterQueueName);
    }

    @Bean
    public Queue housekeepingDeadLetterQueue() {
        return durableDeadLetterQueue(housekeepingDeadLetterQueueName);
    }

    @Bean
    public Queue voucherDeadLetterQueue() {
        return durableDeadLetterQueue(voucherDeadLetterQueueName);
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return durableDeadLetterQueue(emailDeadLetterQueueName);
    }

    @Bean
    public Binding bindingHousekeeping(@Qualifier("housekeepingQueue") Queue queue,
                                       @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(housekeepingRoutingKey);
    }

    @Bean
    public Binding bindingVoucher(@Qualifier("voucherQueue") Queue queue,
                                  @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(voucherRoutingKey);
    }

    @Bean
    public Binding bindingEmail(@Qualifier("emailQueue") Queue queue,
                                TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(notificationsBindingKey);
    }

    @Bean
    public Binding bindingHousekeepingDeadLetter(
            @Qualifier("housekeepingDeadLetterQueue") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(housekeepingDeadLetterQueueName);
    }

    @Bean
    public Binding bindingVoucherDeadLetter(@Qualifier("voucherDeadLetterQueue") Queue queue,
                                            @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(voucherDeadLetterQueueName);
    }

    @Bean
    public Binding bindingEmailDeadLetter(@Qualifier("emailDeadLetterQueue") Queue queue,
                                          @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(emailDeadLetterQueueName);
    }

    private Queue durableWorkQueue(String queueName, String deadLetterQueueName) {
        validateQueuePolicies();
        return QueueBuilder.durable(queueName)
                .withArgument("x-max-length", queueMaxLength)
                .withArgument("x-message-ttl", queueMessageTtlMs)
                .withArgument("x-overflow", queueOverflow)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterQueueName)
                .build();
    }

    private Queue durableDeadLetterQueue(String queueName) {
        validateQueuePolicies();
        return QueueBuilder.durable(queueName)
                .withArgument("x-max-length", deadLetterQueueMaxLength)
                .withArgument("x-message-ttl", deadLetterQueueMessageTtlMs)
                .build();
    }

    private void validateQueuePolicies() {
        if (queueMaxLength < 1
                || queueMessageTtlMs < 1
                || deadLetterQueueMaxLength < 1
                || deadLetterQueueMessageTtlMs < 1
                || !"reject-publish-dlx".equals(queueOverflow)) {
            throw new IllegalStateException("La configuración de retención de colas RabbitMQ es inválida");
        }
    }
}
