package com.marimo.server.global.circuitbreaker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CircuitBreakerConfig {

    // 실패 횟수 임계값 (이 횟수만큼 실패하면 OPEN 상태로 전환)
    private int failureThreshold = 5;

    // 성공 횟수 임계값 (HALF_OPEN 상태에서 이 횟수만큼 성공하면 CLOSED로 전환)
    private int successThreshold = 3;

    // OPEN 상태 유지 시간 (밀리초, 이 시간 후 HALF_OPEN으로 전환)
    private long timeoutDuration = 60000; // 1분

    // 통계 수집을 위한 시간 윈도우 (밀리초)
    private long statisticsWindowDuration = 300000; // 5분
}
