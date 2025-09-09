package com.marimo.server.global.circuitbreaker;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "circuit-breaker")
public record CircuitBreakerConfig(
        int failureThreshold,
        int successThreshold,
        long timeoutDuration
) {

}
