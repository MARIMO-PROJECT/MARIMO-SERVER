package com.marimo.server.global.circuitbreaker;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;

@Getter
public class CircuitBreakerMetrics {

    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong failedCalls = new AtomicLong(0);
    private final AtomicLong successfulCalls = new AtomicLong(0);
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private final AtomicLong consecutiveSuccesses = new AtomicLong(0);

    private volatile Instant lastFailureTime;
    private volatile Instant lastSuccessTime;
    private volatile Instant lastStateChangeTime = Instant.now();

    public void recordSuccess() {
        totalCalls.incrementAndGet();
        successfulCalls.incrementAndGet();
        consecutiveSuccesses.incrementAndGet();
        consecutiveFailures.set(0);
        lastSuccessTime = Instant.now();
    }

    public void recordFailure() {
        totalCalls.incrementAndGet();
        failedCalls.incrementAndGet();
        consecutiveFailures.incrementAndGet();
        consecutiveSuccesses.set(0);
        lastFailureTime = Instant.now();
    }

    public void recordStateChange() {
        lastStateChangeTime = Instant.now();
    }

    public double getFailureRate() {
        long total = totalCalls.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) failedCalls.get() / total;
    }

    public void reset() {
        totalCalls.set(0);
        failedCalls.set(0);
        successfulCalls.set(0);
        consecutiveFailures.set(0);
        consecutiveSuccesses.set(0);
    }
}
