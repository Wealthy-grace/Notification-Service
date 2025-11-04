package com.example.notificationservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class RabbitMQConfig {

    // Queue Names
    @Value("${rabbitmq.queue.notification}")
    private String notificationQueue;

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.queue.property-notification}")
    private String propertyNotificationQueue;

    @Value("${rabbitmq.queue.dlq}")
    private String deadLetterQueue;

    // Exchange Names
    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchange;

    @Value("${rabbitmq.exchange.dlx}")
    private String deadLetterExchange;

    // Routing Keys
    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing-key.property-notification}")
    private String propertyNotificationRoutingKey;

    @Value("${rabbitmq.routing-key.dlq}")
    private String deadLetterRoutingKey;

    // ==================== QUEUES ====================


    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    /**
     * Email-specific queue
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    /**
     * Property notification queue
     */
    @Bean
    public Queue propertyNotificationQueue() {
        return QueueBuilder.durable(propertyNotificationQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    /**
     * Dead Letter Queue for failed messages
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    // ==================== EXCHANGES ====================

    /**
     * Main notification topic exchange
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange);
    }

    /**
     * Dead Letter Exchange
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange);
    }

    // ==================== BINDINGS ====================

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(notificationRoutingKey);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(notificationExchange())
                .with(emailRoutingKey);
    }

    @Bean
    public Binding propertyNotificationBinding() {
        return BindingBuilder
                .bind(propertyNotificationQueue())
                .to(notificationExchange())
                .with(propertyNotificationRoutingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
    }

    // ==================== MESSAGE CONVERTER ====================

    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ==================== LISTENER CONTAINER FACTORY ====================


    @Bean
    @Primary
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        // Set connection factory
        factory.setConnectionFactory(connectionFactory);

        // Use JSON message converter
        factory.setMessageConverter(jsonMessageConverter);

        // Concurrent consumers (how many listeners per queue)
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);

        // Prefetch count (how many messages to fetch at once)
        factory.setPrefetchCount(1);

        // Auto-acknowledge messages after successful processing
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        // ✅ CRITICAL: Enable auto-startup of listeners!
        // Without this, @RabbitListener methods will NOT start consuming!
        factory.setAutoStartup(true);

        return factory;
    }
}