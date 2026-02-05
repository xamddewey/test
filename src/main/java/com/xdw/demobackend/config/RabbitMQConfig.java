package com.xdw.demobackend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 * 
 * Defines 4 exchanges and 4 queues for asynchronous processing:
 * 1. Notifications - User notification events
 * 2. Audit - Entity change audit logs
 * 3. Settlement - Settlement calculation triggers
 * 4. Statistics - Statistics aggregation triggers
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String NOTIFICATIONS_EXCHANGE = "ledger.notifications";
    public static final String AUDIT_EXCHANGE = "ledger.audit";
    public static final String SETTLEMENT_EXCHANGE = "ledger.settlement";
    public static final String STATISTICS_EXCHANGE = "ledger.statistics";

    // Queue names
    public static final String NOTIFICATION_QUEUE = "notification-queue";
    public static final String AUDIT_QUEUE = "audit-queue";
    public static final String SETTLEMENT_QUEUE = "settlement-queue";
    public static final String STATISTICS_QUEUE = "statistics-queue";

    // Routing keys
    public static final String NOTIFICATION_ROUTING_KEY = "notification.*";
    public static final String AUDIT_ROUTING_KEY = "audit.*.*";
    public static final String SETTLEMENT_ROUTING_KEY = "settlement.trigger";
    public static final String STATISTICS_ROUTING_KEY = "statistics.*";

    /**
     * JSON message converter for RabbitMQ
     * Serializes/deserializes messages as JSON using Jackson
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ========================================
    // Notification Exchange and Queue
    // ========================================

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(NOTIFICATIONS_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true); // durable = true
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationsExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // ========================================
    // Audit Exchange and Queue
    // ========================================

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true); // durable = true
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue)
                .to(auditExchange)
                .with(AUDIT_ROUTING_KEY);
    }

    // ========================================
    // Settlement Exchange and Queue
    // ========================================

    @Bean
    public TopicExchange settlementExchange() {
        return new TopicExchange(SETTLEMENT_EXCHANGE);
    }

    @Bean
    public Queue settlementQueue() {
        return new Queue(SETTLEMENT_QUEUE, true); // durable = true
    }

    @Bean
    public Binding settlementBinding(Queue settlementQueue, TopicExchange settlementExchange) {
        return BindingBuilder.bind(settlementQueue)
                .to(settlementExchange)
                .with(SETTLEMENT_ROUTING_KEY);
    }

    // ========================================
    // Statistics Exchange and Queue
    // ========================================

    @Bean
    public TopicExchange statisticsExchange() {
        return new TopicExchange(STATISTICS_EXCHANGE);
    }

    @Bean
    public Queue statisticsQueue() {
        return new Queue(STATISTICS_QUEUE, true); // durable = true
    }

    @Bean
    public Binding statisticsBinding(Queue statisticsQueue, TopicExchange statisticsExchange) {
        return BindingBuilder.bind(statisticsQueue)
                .to(statisticsExchange)
                .with(STATISTICS_ROUTING_KEY);
    }
}
