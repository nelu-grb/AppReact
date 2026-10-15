package com.react.backend.msnofity.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de RabbitMQ de msnofity. Aquí SOLO se declara infraestructura
 * (exchanges, colas, bindings y políticas de retención); la lógica de negocio
 * vive en los services y la de consumo en los listeners.
 *
 * Todos los nombres, routing keys y límites se leen de application.properties
 * (prefijo app.rabbitmq.*), sin valores sueltos en el código.
 *
 * Topología:
 *   cmd.direct (direct) --email.send-------> q.cmd.email         --(DLX)--> q.cmd.email.dlq
 *                       --housekeeping.ticket-> q.cmd.housekeeping --(DLX)--> q.cmd.housekeeping.dlq
 *                       --voucher.gen-------> q.cmd.voucher       --(DLX)--> q.cmd.voucher.dlq
 *   evt.topic  (topic)  --reservation.*-----> q.evt.notify   (durable)
 *                       --#------------------> cola anónima de monitoreo (temporal)
 */
@Configuration
public class RabbitMQConfig {

    // ---------- Exchanges ----------
    @Value("${app.rabbitmq.exchange.direct}")
    private String directExchangeName;

    @Value("${app.rabbitmq.exchange.events}")
    private String eventsExchangeName;

    @Value("${app.rabbitmq.exchange.dlx}")
    private String deadLetterExchangeName;

    // ---------- Colas ----------
    @Value("${app.rabbitmq.queue.email}")
    private String emailQueueName;

    @Value("${app.rabbitmq.queue.housekeeping}")
    private String housekeepingQueueName;

    @Value("${app.rabbitmq.queue.voucher}")
    private String voucherQueueName;

    @Value("${app.rabbitmq.queue.email-dlq}")
    private String emailDlqName;

    @Value("${app.rabbitmq.queue.housekeeping-dlq}")
    private String housekeepingDlqName;

    @Value("${app.rabbitmq.queue.voucher-dlq}")
    private String voucherDlqName;

    @Value("${app.rabbitmq.queue.evt-notify}")
    private String evtNotifyQueueName;

    // ---------- Routing keys / patrones ----------
    @Value("${app.rabbitmq.routing.email}")
    private String emailRoutingKey;

    @Value("${app.rabbitmq.routing.housekeeping}")
    private String housekeepingRoutingKey;

    @Value("${app.rabbitmq.routing.voucher}")
    private String voucherRoutingKey;

    @Value("${app.rabbitmq.routing.events-notify}")
    private String eventsNotifyRoutingKey;

    @Value("${app.rabbitmq.routing.events-monitor}")
    private String eventsMonitorRoutingKey;

    // ---------- Políticas de retención y limpieza ----------
    @Value("${app.rabbitmq.retention.queue-message-ttl-ms}")
    private int queueMessageTtlMs;

    @Value("${app.rabbitmq.retention.queue-max-length}")
    private int queueMaxLength;

    @Value("${app.rabbitmq.retention.dlq-message-ttl-ms}")
    private int dlqMessageTtlMs;

    @Value("${app.rabbitmq.retention.dlq-max-length}")
    private int dlqMaxLength;

    @Value("${app.rabbitmq.retention.events-message-ttl-ms}")
    private int eventsMessageTtlMs;

    @Value("${app.rabbitmq.retention.events-max-length}")
    private int eventsMaxLength;

    // =====================================================================
    // Exchanges
    // =====================================================================

    /** Comandos: routing key exacta, un solo consumidor por mensaje. */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchangeName, true, false);
    }

    /** Eventos: routing key con patrones (*, #), varios interesados por evento. */
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchangeName, true, false);
    }

    /** Dead Letter Exchange: recibe los mensajes rechazados, vencidos o desbordados. */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName, true, false);
    }

    // =====================================================================
    // Colas de comandos (cada una con su DLQ)
    // =====================================================================

    @Bean
    public Queue emailQueue() {
        return commandQueue(emailQueueName, emailDlqName);
    }

    @Bean
    public Queue housekeepingQueue() {
        return commandQueue(housekeepingQueueName, housekeepingDlqName);
    }

    @Bean
    public Queue voucherQueue() {
        return commandQueue(voucherQueueName, voucherDlqName);
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return deadLetterQueue(emailDlqName);
    }

    @Bean
    public Queue housekeepingDeadLetterQueue() {
        return deadLetterQueue(housekeepingDlqName);
    }

    @Bean
    public Queue voucherDeadLetterQueue() {
        return deadLetterQueue(voucherDlqName);
    }

    // =====================================================================
    // Bindings de comandos (direct)
    // =====================================================================

    @Bean
    public Binding bindingEmail() {
        return BindingBuilder.bind(emailQueue()).to(directExchange()).with(emailRoutingKey);
    }

    @Bean
    public Binding bindingHousekeeping() {
        return BindingBuilder.bind(housekeepingQueue()).to(directExchange()).with(housekeepingRoutingKey);
    }

    @Bean
    public Binding bindingVoucher() {
        return BindingBuilder.bind(voucherQueue()).to(directExchange()).with(voucherRoutingKey);
    }

    // =====================================================================
    // Bindings de las DLQ (la routing key es el nombre de la propia DLQ,
    // la misma que se declara en x-dead-letter-routing-key de la cola origen)
    // =====================================================================

    @Bean
    public Binding bindingEmailDeadLetter() {
        return BindingBuilder.bind(emailDeadLetterQueue()).to(deadLetterExchange()).with(emailDlqName);
    }

    @Bean
    public Binding bindingHousekeepingDeadLetter() {
        return BindingBuilder.bind(housekeepingDeadLetterQueue()).to(deadLetterExchange()).with(housekeepingDlqName);
    }

    @Bean
    public Binding bindingVoucherDeadLetter() {
        return BindingBuilder.bind(voucherDeadLetterQueue()).to(deadLetterExchange()).with(voucherDlqName);
    }

    // =====================================================================
    // Eventos (topic)
    // =====================================================================

    /** Cola durable: conserva los eventos aunque msnofity esté caído. */
    @Bean
    public Queue evtNotifyQueue() {
        return QueueBuilder.durable(evtNotifyQueueName)
                .ttl(eventsMessageTtlMs)
                .maxLength(eventsMaxLength)
                .overflow(QueueBuilder.Overflow.dropHead)
                .build();
    }

    @Bean
    public Binding evtNotifyBinding() {
        return BindingBuilder.bind(evtNotifyQueue()).to(eventsExchange()).with(eventsNotifyRoutingKey);
    }

    /** Cola temporal de monitoreo: se crea al iniciar y se borra al desconectarse. */
    @Bean
    public Queue evtMonitorQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding evtMonitorBinding() {
        return BindingBuilder.bind(evtMonitorQueue()).to(eventsExchange()).with(eventsMonitorRoutingKey);
    }

    // =====================================================================
    // Fábricas de colas con política de retención
    // =====================================================================

    /**
     * Cola de comandos: durable, con DLX y límites de vida y tamaño.
     * Si un mensaje vence (TTL) o desborda el máximo (se descarta el más antiguo,
     * drop-head), RabbitMQ lo reenvía al DLX y termina en la DLQ de la cola.
     */
    private Queue commandQueue(String name, String dlqName) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(dlqName)
                .ttl(queueMessageTtlMs)
                .maxLength(queueMaxLength)
                .overflow(QueueBuilder.Overflow.dropHead)
                .build();
    }

    /**
     * DLQ: durable y con límites propios. No define DLX, así que lo que venza
     * o desborde aquí se descarta: la DLQ no puede crecer sin control.
     */
    private Queue deadLetterQueue(String name) {
        return QueueBuilder.durable(name)
                .ttl(dlqMessageTtlMs)
                .maxLength(dlqMaxLength)
                .overflow(QueueBuilder.Overflow.dropHead)
                .build();
    }
}