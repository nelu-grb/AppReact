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
        return QueueBuilder.durable(housekeepingQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", housekeepingDeadLetterQueueName)
                .build();
    }

    @Bean
    public Queue voucherQueue() {
        return QueueBuilder.durable(voucherQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", voucherDeadLetterQueueName)
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", emailDeadLetterQueueName)
                .build();
    }

    @Bean
    public Queue housekeepingDeadLetterQueue() {
        return QueueBuilder.durable(housekeepingDeadLetterQueueName).build();
    }

    @Bean
    public Queue voucherDeadLetterQueue() {
        return QueueBuilder.durable(voucherDeadLetterQueueName).build();
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(emailDeadLetterQueueName).build();
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
}
