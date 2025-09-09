package com.marimo.server.global.circuitbreaker;

import lombok.Getter;

@Getter
public class CircuitBreakerMetrics {

    private long totalCallCount;
    private long failureCallCount;
    private long successCallCount;
    private long failureStreak;
    private long successStreak;

    private long lastFailureAtMillis;
    private long lastSuccessAtMillis;
    private long lastStateChangeTimeMillis = System.currentTimeMillis();

    public void recordSuccess() {
        totalCallCount++;
        successCallCount++;
        successStreak++;
        failureStreak = 0;
        lastSuccessAtMillis = System.currentTimeMillis();
    }

    public void recordFailure() {
        totalCallCount++;
        failureCallCount++;
        failureStreak++;
        successStreak = 0;
        lastFailureAtMillis = System.currentTimeMillis();
    }

    public void recordStateChange() {
        lastStateChangeTimeMillis = System.currentTimeMillis();
    }

    public double failureRatio() {
        return (totalCallCount == 0) ? 0.0 : (double) failureCallCount / totalCallCount;
    }

    public double failureRatePercent() {
        return failureRatio() * 100.0;
    }

    public void reset() {
        totalCallCount = 0;
        failureCallCount = 0;
        successCallCount = 0;
        failureStreak = 0;
        successStreak = 0;
    }
}
