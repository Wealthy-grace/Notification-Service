package com.example.notificationservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Circuit Breaker Health and Monitoring Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/circuit-breaker")
@RequiredArgsConstructor
public class CircuitBreakerHealthController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Get status of all circuit breakers
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllCircuitBreakersStatus() {
        log.info("📊 Fetching all circuit breaker statuses");

        Map<String, Object> response = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .collect(Collectors.toMap(
                        CircuitBreaker::getName,
                        this::getCircuitBreakerDetails
                ));

        return ResponseEntity.ok(response);
    }

    /**
     * Get status of specific circuit breaker
     */
    @GetMapping("/status/{name}")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus(@PathVariable String name) {
        log.info("📊 Fetching circuit breaker status for: {}", name);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
            Map<String, Object> details = getCircuitBreakerDetails(circuitBreaker);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("❌ Circuit breaker not found: {}", name);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Manually transition circuit breaker to CLOSED state
     */
    @PostMapping("/reset/{name}")
    public ResponseEntity<Map<String, String>> resetCircuitBreaker(@PathVariable String name) {
        log.info("🔄 Manually resetting circuit breaker: {}", name);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
            circuitBreaker.transitionToClosedState();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Circuit breaker reset successfully");
            response.put("name", name);
            response.put("newState", circuitBreaker.getState().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to reset circuit breaker: {}", name);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get detailed metrics for a circuit breaker
     */
    private Map<String, Object> getCircuitBreakerDetails(CircuitBreaker cb) {
        Map<String, Object> details = new HashMap<>();
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        details.put("name", cb.getName());
        details.put("state", cb.getState().toString());
        details.put("failureRate", String.format("%.2f%%", metrics.getFailureRate()));
        details.put("slowCallRate", String.format("%.2f%%", metrics.getSlowCallRate()));
        details.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
        details.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        details.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        details.put("numberOfSlowCalls", metrics.getNumberOfSlowCalls());
        details.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());

        return details;
    }
}