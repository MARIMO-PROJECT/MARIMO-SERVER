package com.marimo.server.global.circuitbreaker;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CircuitBreakerStatus {

    private final CircuitBreakerState state;
    private final long totalCalls;
    private final long failedCalls;
    private final long successfulCalls;
    private final long consecutiveFailures;
    private final long consecutiveSuccesses;
    private final double failureRate;
    private final Instant lastFailureTime;
    private final Instant lastSuccessTime;
    private final Instant lastStateChangeTime;

    public String getStateDescription() {
        return switch (state) {
            case CLOSED -> "🟢 정상 - Slack 알림 활성화";
            case OPEN -> "🔴 장애 - Discord 폴백 모드";
            case HALF_OPEN -> "🟡 회복 테스트 - Slack 재시도 중";
        };
    }

    public boolean isUsingFallback() {
        return state == CircuitBreakerState.OPEN;
    }
}
