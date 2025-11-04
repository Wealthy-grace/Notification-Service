package com.example.notificationservice.config;


import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Test configuration to provide mock beans for RabbitMQ components.
 * This prevents the application from trying to connect to a real RabbitMQ instance during tests.
 */
@TestConfiguration
@Profile("test")
public class TestRabbitMQConfiguration {

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return mock(ConnectionFactory.class);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return mock(RabbitAdmin.class);
    }
}