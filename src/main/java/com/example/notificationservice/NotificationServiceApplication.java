package com.example.notificationservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableRabbit
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.notificationservice",
        "com.example.notificationservice.consumer",  // Explicitly scan consumer package
        "com.example.notificationservice.configuration"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}