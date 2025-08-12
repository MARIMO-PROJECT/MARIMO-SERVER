package com.marimo.server.global.notification;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.marimo.server.global.circuitbreaker.CircuitBreakerStatus;
import com.marimo.server.global.logging.FallbackNotificationAppender;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationTestController {

    private final NotificationHealthCheckService healthCheckService;

    @PostMapping("/error")
    public ResponseEntity<String> testErrorNotification() {
        log.error("🧪 테스트 에러 알림 - 이것은 테스트용 에러입니다!");
        return ResponseEntity.ok("테스트 에러 알림이 전송되었습니다.");
    }

    @PostMapping("/exception")
    public ResponseEntity<String> testExceptionNotification() {
        try {
            throw new RuntimeException("테스트용 예외입니다!");
        } catch (Exception e) {
            log.error("🧪 테스트 예외 알림", e);
        }
        return ResponseEntity.ok("테스트 예외 알림이 전송되었습니다.");
    }

    @PostMapping("/health-check")
    public ResponseEntity<String> triggerHealthCheck() {
        try {
            healthCheckService.performHealthCheck();
            return ResponseEntity.ok("Health Check가 수동으로 실행되었습니다.");
        } catch (Exception e) {
            log.error("Health Check 수동 실행 중 에러 발생", e);
            return ResponseEntity.status(500).body("Health Check 실행 중 에러가 발생했습니다.");
        }
    }

    @GetMapping("/circuit-breaker/status")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

            FallbackNotificationAppender fallbackAppender = null;
            var appenderIterator = rootLogger.iteratorForAppenders();

            while (appenderIterator.hasNext()) {
                var appender = appenderIterator.next();
                if (appender.getName().equals("ASYNC_FALLBACK")) {
                    ch.qos.logback.classic.AsyncAppender asyncAppender =
                            (ch.qos.logback.classic.AsyncAppender) appender;

                    var asyncAppenderIterator = asyncAppender.iteratorForAppenders();
                    while (asyncAppenderIterator.hasNext()) {
                        var subAppender = asyncAppenderIterator.next();
                        if (subAppender instanceof FallbackNotificationAppender) {
                            fallbackAppender = (FallbackNotificationAppender) subAppender;
                            break;
                        }
                    }
                    break;
                }
            }

            if (fallbackAppender == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "FallbackNotificationAppender를 찾을 수 없습니다."
                ));
            }

            CircuitBreakerStatus status = fallbackAppender.getCircuitBreaker().getStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("state", status.getState());
            response.put("totalCalls", status.getTotalCalls());
            response.put("failedCalls", status.getFailedCalls());
            response.put("successfulCalls", status.getSuccessfulCalls());
            response.put("consecutiveFailures", status.getConsecutiveFailures());
            response.put("consecutiveSuccesses", status.getConsecutiveSuccesses());
            response.put("failureRate", status.getFailureRate());
            response.put("lastFailureTime", status.getLastFailureTime());
            response.put("lastSuccessTime", status.getLastSuccessTime());
            response.put("lastStateChangeTime", status.getLastStateChangeTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Circuit Breaker 상태 조회 중 에러 발생", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Circuit Breaker 상태 조회 중 에러가 발생했습니다."
            ));
        }
    }

    @PostMapping("/circuit-breaker/reset")
    public ResponseEntity<String> resetCircuitBreaker() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

            FallbackNotificationAppender fallbackAppender = null;
            var appenderIterator = rootLogger.iteratorForAppenders();

            while (appenderIterator.hasNext()) {
                var appender = appenderIterator.next();
                if (appender.getName().equals("ASYNC_FALLBACK")) {
                    ch.qos.logback.classic.AsyncAppender asyncAppender =
                            (ch.qos.logback.classic.AsyncAppender) appender;

                    var asyncAppenderIterator = asyncAppender.iteratorForAppenders();
                    while (asyncAppenderIterator.hasNext()) {
                        var subAppender = asyncAppenderIterator.next();
                        if (subAppender instanceof FallbackNotificationAppender) {
                            fallbackAppender = (FallbackNotificationAppender) subAppender;
                            break;
                        }
                    }
                    break;
                }
            }

            if (fallbackAppender == null) {
                return ResponseEntity.status(404).body("FallbackNotificationAppender를 찾을 수 없습니다.");
            }

            fallbackAppender.getCircuitBreaker().reset();
            return ResponseEntity.ok("Circuit Breaker가 리셋되었습니다.");

        } catch (Exception e) {
            log.error("Circuit Breaker 리셋 중 에러 발생", e);
            return ResponseEntity.status(500).body("Circuit Breaker 리셋 중 에러가 발생했습니다.");
        }
    }

    @GetMapping("/failed-notifications/count")
    public ResponseEntity<Map<String, Object>> getFailedNotificationCount() {
        // 실제로는 FailedNotificationAppender에서 통계를 가져와야 하지만,
        // 간소화를 위해 현재는 기본 응답만 제공
        Map<String, Object> response = new HashMap<>();
        response.put("message", "실패 알림 통계 조회 기능은 추후 구현 예정입니다.");
        response.put("note", "현재는 로그 파일에서 직접 확인할 수 있습니다: logs/failed-notifications/");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/multi-error-test")
    public ResponseEntity<String> testMultipleErrors() {
        log.info("다중 에러 테스트 - Circuit Breaker 상태 전환 테스트");

        // Circuit Breaker를 OPEN 상태로 만들기 위한 연속 에러 생성
        for (int i = 1; i <= 6; i++) {
            try {
                throw new RuntimeException("🔄 Circuit Breaker 테스트 에러 #" + i + " - " +
                        "5회 실패 후 OPEN 상태로 전환됩니다.");
            } catch (Exception e) {
                log.error("테스트 에러 #{}: {}", i, e.getMessage(), e);
            }

            // 약간의 지연
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return ResponseEntity.ok("✅ 다중 에러 테스트 완료. Circuit Breaker 상태를 확인하세요.");
    }

    @PostMapping("/custom-error")
    public ResponseEntity<String> testCustomError() {
        log.info("커스텀 에러 테스트 API 호출됨");
        try {
            // 의도적으로 NullPointerException 발생
            String nullString = null;
            int length = nullString.length();
        } catch (Exception e) {
            log.error("커스텀 에러가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("NullPointerException으로 인한 에러입니다.", e);
        }
        return ResponseEntity.ok("이 코드는 실행되지 않습니다.");
    }

    @PostMapping("/order-notification")
    public ResponseEntity<String> testOrderNotification(
            @RequestParam(defaultValue = "청첩장") String orderType,
            @RequestParam(defaultValue = "테스트고객") String customerName
    ) {
        try {
            // ORDER_NOTIFICATION 로거를 직접 사용하여 테스트 알림 전송
            org.slf4j.Logger orderLogger = org.slf4j.LoggerFactory.getLogger("ORDER_NOTIFICATION");
            String message = String.format("🧪 [주문 성공] 테스트 주문 알림 - %s 주문\n" +
                            "📋 주문자: %s\n" +
                            "⚡ 알림 경로: Slack → Discord → 파일저장 우선순위로 전송됩니다.",
                    orderType,
                    customerName
            );
            orderLogger.info(message);

            return ResponseEntity.ok("주문 알림 테스트가 전송되었습니다. (" + orderType + " - " + customerName + ")");
        } catch (Exception e) {
            log.error("주문 알림 테스트 중 에러 발생", e);
            return ResponseEntity.status(500).body("주문 알림 테스트 중 에러가 발생했습니다.");
        }
    }
}
