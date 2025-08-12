package com.marimo.server.global.circuitbreaker;

public enum CircuitBreakerState {
    CLOSED,    // 정상 상태 - Primary (Slack) 사용
    OPEN,      // 장애 상태 - Fallback (Discord)으로 우회
    HALF_OPEN  // 회복 테스트 상태 - Primary 재시도 중
}
