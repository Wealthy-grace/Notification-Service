package com.example.notificationservice.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker Configuration for Notification Service
 * Provides fault tolerance when calling external services
 */
@Slf4j
@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

        // Configure User Service Circuit Breaker with event listeners
        CircuitBreaker userServiceCB = registry.circuitBreaker("userService");
        userServiceCB.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("🔄 USER SERVICE Circuit Breaker: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onError(event ->
                        log.error("❌ USER SERVICE Circuit Breaker ERROR: {}",
                                event.getThrowable().getMessage()))
                .onSuccess(event ->
                        log.debug("✅ USER SERVICE Circuit Breaker: Call successful"))
                .onCallNotPermitted(event ->
                        log.warn("🚫 USER SERVICE Circuit Breaker: Call not permitted (Circuit is OPEN)"))
                .onSlowCallRateExceeded(event ->
                        log.warn("🐌 USER SERVICE Circuit Breaker: Slow call rate exceeded"));

        // Configure Property Service Circuit Breaker with event listeners
        CircuitBreaker propertyServiceCB = registry.circuitBreaker("propertyService");
        propertyServiceCB.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("🔄 PROPERTY SERVICE Circuit Breaker: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onError(event ->
                        log.error("❌ PROPERTY SERVICE Circuit Breaker ERROR: {}",
                                event.getThrowable().getMessage()))
                .onSuccess(event ->
                        log.debug("✅ PROPERTY SERVICE Circuit Breaker: Call successful"))
                .onCallNotPermitted(event ->
                        log.warn("🚫 PROPERTY SERVICE Circuit Breaker: Call not permitted (Circuit is OPEN)"))
                .onSlowCallRateExceeded(event ->
                        log.warn("🐌 PROPERTY SERVICE Circuit Breaker: Slow call rate exceeded"));

        return registry;
    }
}