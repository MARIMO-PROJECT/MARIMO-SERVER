package com.marimo.server.global.circuitbreaker;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SlackCircuitBreaker {

    private final CircuitBreakerConfig config;
    private final CircuitBreakerMetrics metrics = new CircuitBreakerMetrics();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private long lastOpenAtMillis;

    public boolean canExecute() {
        boolean shouldTransitionToHalfOpen = false;

        lock.readLock().lock();
        try {
            if (state == CircuitBreakerState.OPEN && hasOpenTimeoutElapsed()) {
                shouldTransitionToHalfOpen = true;
            }
        } finally {
            lock.readLock().unlock();
        }

        if (shouldTransitionToHalfOpen) {
            lock.writeLock().lock();
            try {
                if (state == CircuitBreakerState.OPEN && hasOpenTimeoutElapsed()) {
                    transitionTo(CircuitBreakerState.HALF_OPEN);
                    log.info("🟡 Slack CB: OPEN → HALF_OPEN (타임아웃 {}ms 경과)", config.timeoutDuration());
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        lock.readLock().lock();
        try {
            return state == CircuitBreakerState.CLOSED || state == CircuitBreakerState.HALF_OPEN;
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean hasOpenTimeoutElapsed() {
        return lastOpenAtMillis > 0 && (System.currentTimeMillis() - lastOpenAtMillis) >= config.timeoutDuration();
    }

    public void recordSuccess() {
        lock.writeLock().lock();
        try {
            metrics.recordSuccess();

            if (state == CircuitBreakerState.HALF_OPEN && metrics.getSuccessStreak() >= config.successThreshold()) {
                transitionTo(CircuitBreakerState.CLOSED);
                log.info("🟢 Slack CB: HALF_OPEN → CLOSED (연속 {}회 성공)", metrics.getSuccessStreak());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void recordFailure() {
        lock.writeLock().lock();
        try {
            metrics.recordFailure();

            if (state == CircuitBreakerState.HALF_OPEN) {
                transitionTo(CircuitBreakerState.OPEN);
                lastOpenAtMillis = System.currentTimeMillis();
                log.warn("🔴 Slack CB: HALF_OPEN → OPEN (테스트 실패)");

                return;
            }

            if (state == CircuitBreakerState.CLOSED && metrics.getFailureStreak() >= config.failureThreshold()) {
                transitionTo(CircuitBreakerState.OPEN);
                lastOpenAtMillis = System.currentTimeMillis();
                log.warn("🔴 Slack CB: CLOSED → OPEN (연속 {}회 실패), Discord로 fallback 시작", metrics.getFailureStreak());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 항상 write 락을 보유한 상태에서만 호출되어야 함
    private void transitionTo(final CircuitBreakerState newState) {
        this.state = newState;
        metrics.recordStateChange();

        if (newState == CircuitBreakerState.CLOSED) {
            metrics.reset();
        }
    }

    public CircuitBreakerStatus getStatus() {
        lock.readLock().lock();
        try {
            return CircuitBreakerStatus.builder()
                    .state(state)
                    .metrics(metrics)
                    .build();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void reset() {
        lock.writeLock().lock();
        try {
            transitionTo(CircuitBreakerState.CLOSED);
            metrics.reset();
            lastOpenAtMillis = 0L;
        } finally {
            lock.writeLock().unlock();
            log.info("🔄 Slack CB: 강제 리셋 완료");
        }
    }
}
