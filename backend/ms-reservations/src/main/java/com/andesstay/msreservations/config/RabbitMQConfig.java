package com.andesstay.msreservations.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // intercambios de mensajes.
    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";
    public static final String EVT_EXCHANGE = "evt.fanout";

    // colas para procesar los comandos.
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_HOUSEKEEPING = "q.cmd.housekeeping";
    public static final String QUEUE_VOUCHER = "q.cmd.voucher";
    public static final String QUEUE_EMAIL_DLQ = QUEUE_EMAIL + ".dlq";
    public static final String QUEUE_HOUSEKEEPING_DLQ = QUEUE_HOUSEKEEPING + ".dlq";
    public static final String QUEUE_VOUCHER_DLQ = QUEUE_VOUCHER + ".dlq";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false); // se mantiene en el broker.
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // intercambia eventos con todos los servicios suscritos.
    @Bean
    public FanoutExchange evtExchange() {
        return new FanoutExchange(EVT_EXCHANGE, true, false);
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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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
