package com.react.backend.msnofity.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";
    public static final String EVT_EXCHANGE = "evt.fanout";
    public static final String EVT_NOTIFY_QUEUE = "q.evt.notify";

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

    // Direct Exchange y DLX durables
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // Exchange Fanout para Pub/Sub (debe coincidir con ms-reservations)
    @Bean
    public FanoutExchange evtExchange() {
        return new FanoutExchange(EVT_EXCHANGE, true, false);
    }

    // Cola durable fija para recibir eventos Pub/Sub (persiste si msnofity está caído)
    @Bean
    public Queue evtNotifyQueue() {
        return QueueBuilder.durable(EVT_NOTIFY_QUEUE).build();
    }

    @Bean
    public Binding evtNotifyBinding(FanoutExchange evtExchange, Queue evtNotifyQueue) {
        return BindingBuilder.bind(evtNotifyQueue).to(evtExchange);
    }

    // Cola TEMPORAL para monitoreo (se crea al iniciar y se borra al desconectarse)
    @Bean
    public Queue evtMonitorQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding evtMonitorBinding(FanoutExchange evtExchange, Queue evtMonitorQueue) {
        return BindingBuilder.bind(evtMonitorQueue).to(evtExchange);
    }

    // Definición de Colas de Comandos
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
}