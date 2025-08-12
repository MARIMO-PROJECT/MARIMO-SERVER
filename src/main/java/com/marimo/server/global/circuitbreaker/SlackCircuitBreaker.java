package com.marimo.server.global.circuitbreaker;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackCircuitBreaker {

    private final CircuitBreakerConfig config;
    private final CircuitBreakerMetrics metrics;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private volatile Instant lastOpenTime;

    public SlackCircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
        this.metrics = new CircuitBreakerMetrics();
    }

    /**
     * Slack 알림 시도 전 Circuit Breaker 상태 확인
     *
     * @return true if should use Slack (primary), false if should use Discord (fallback)
     */
    public boolean canExecute() {
        lock.readLock().lock();
        try {
            CircuitBreakerState currentState = getCurrentState();

            switch (currentState) {
                case CLOSED:
                    return true; // Slack 사용 가능
                case OPEN:
                    return false; // Discord로 fallback
                case HALF_OPEN:
                    // HALF_OPEN 상태에서는 제한적으로 Slack 시도
                    return true;
                default:
                    return false;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Slack 알림 성공 시 호출
     */
    public void recordSuccess() {
        lock.writeLock().lock();
        try {
            metrics.recordSuccess();

            CircuitBreakerState currentState = getCurrentState();

            if (currentState == CircuitBreakerState.HALF_OPEN) {
                // HALF_OPEN 상태에서 연속 성공이 임계값에 도달하면 CLOSED로 전환
                if (metrics.getConsecutiveSuccesses().get() >= config.getSuccessThreshold()) {
                    transitionTo(CircuitBreakerState.CLOSED);
                    log.info("🟢 Slack Circuit Breaker: HALF_OPEN → CLOSED (연속 성공 {}회)",
                            metrics.getConsecutiveSuccesses().get());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Slack 알림 실패 시 호출
     */
    public void recordFailure() {
        lock.writeLock().lock();
        try {
            metrics.recordFailure();

            CircuitBreakerState currentState = getCurrentState();

            if (currentState == CircuitBreakerState.CLOSED || currentState == CircuitBreakerState.HALF_OPEN) {
                // 연속 실패가 임계값에 도달하면 OPEN으로 전환
                if (metrics.getConsecutiveFailures().get() >= config.getFailureThreshold()) {
                    transitionTo(CircuitBreakerState.OPEN);
                    log.warn("🔴 Slack Circuit Breaker: {} → OPEN (연속 실패 {}회), Discord로 fallback 시작",
                            currentState, metrics.getConsecutiveFailures().get());
                    lastOpenTime = Instant.now();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 현재 상태를 반환 (시간 기반 상태 전환 포함)
     */
    private CircuitBreakerState getCurrentState() {
        if (state == CircuitBreakerState.OPEN && lastOpenTime != null) {
            long timeSinceOpened = Instant.now().toEpochMilli() - lastOpenTime.toEpochMilli();

            if (timeSinceOpened >= config.getTimeoutDuration()) {
                transitionTo(CircuitBreakerState.HALF_OPEN);
                log.info("🟡 Slack Circuit Breaker: OPEN → HALF_OPEN (타임아웃 {}ms 경과)",
                        config.getTimeoutDuration());
            }
        }

        return state;
    }

    private void transitionTo(CircuitBreakerState newState) {
        CircuitBreakerState oldState = this.state;
        this.state = newState;
        metrics.recordStateChange();

        if (newState == CircuitBreakerState.CLOSED) {
            metrics.reset(); // CLOSED로 전환 시 메트릭 리셋
        }

        if (oldState != newState) {
            log.info("Circuit Breaker 상태 변경: {} → {}", oldState, newState);
        }
    }

    /**
     * Circuit Breaker 상태 정보 조회
     */
    public CircuitBreakerStatus getStatus() {
        lock.readLock().lock();
        try {
            return CircuitBreakerStatus.builder()
                    .state(getCurrentState())
                    .totalCalls(metrics.getTotalCalls().get())
                    .failedCalls(metrics.getFailedCalls().get())
                    .successfulCalls(metrics.getSuccessfulCalls().get())
                    .consecutiveFailures(metrics.getConsecutiveFailures().get())
                    .consecutiveSuccesses(metrics.getConsecutiveSuccesses().get())
                    .failureRate(metrics.getFailureRate())
                    .lastFailureTime(metrics.getLastFailureTime())
                    .lastSuccessTime(metrics.getLastSuccessTime())
                    .lastStateChangeTime(metrics.getLastStateChangeTime())
                    .build();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Circuit Breaker 강제 리셋 (관리용)
     */
    public void reset() {
        lock.writeLock().lock();
        try {
            transitionTo(CircuitBreakerState.CLOSED);
            metrics.reset();
            lastOpenTime = null;
            log.info("🔄 Slack Circuit Breaker 강제 리셋 완료");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
