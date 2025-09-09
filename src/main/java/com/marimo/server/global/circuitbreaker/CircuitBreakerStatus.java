package com.marimo.server.global.circuitbreaker;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CircuitBreakerStatus {

    private final CircuitBreakerState state;
    private final CircuitBreakerMetrics metrics;

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
