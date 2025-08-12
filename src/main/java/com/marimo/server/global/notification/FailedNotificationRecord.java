package com.marimo.server.global.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailedNotificationRecord {

    private final String id;                    // 고유 ID (UUID)
    private final Instant timestamp;            // 실패 시각
    private final String traceId;               // 요청 추적 ID
    private final String logLevel;              // 로그 레벨 (ERROR, WARN)
    private final String loggerName;            // 로거 이름
    private final String message;               // 에러 메시지
    private final String exceptionClass;       // 예외 클래스명
    private final String exceptionMessage;     // 예외 메시지
    private final String stackTrace;           // 스택트레이스

    // MDC 정보
    private final String clientIp;
    private final String requestURI;
    private final String requestParams;
    private final String requestHeaders;
    private final String requestBody;

    // 실패 정보
    private final String failureReason;        // 실패 사유 ("SLACK_FAILED", "DISCORD_FAILED", "ALL_FAILED")
    private final boolean slackFailed;         // Slack 실패 여부
    private final boolean discordFailed;       // Discord 실패 여부

    // 재시도 정보
    private final int retryCount;              // 재시도 횟수
    private final Instant lastRetryTime;      // 마지막 재시도 시각
    private final boolean processed;          // 처리 완료 여부

    public static FailedNotificationRecord fromLoggingEvent(
            ch.qos.logback.classic.spi.ILoggingEvent event,
            String failureReason,
            boolean slackFailed,
            boolean discordFailed
    ) {
        Map<String, String> mdcMap = event.getMDCPropertyMap();

        String exceptionClass = null;
        String exceptionMessage = null;
        String stackTrace = null;

        if (event.getThrowableProxy() != null) {
            exceptionClass = event.getThrowableProxy().getClassName();
            exceptionMessage = event.getThrowableProxy().getMessage();
            stackTrace = buildStackTrace(event.getThrowableProxy());
        }

        return FailedNotificationRecord.builder()
                .id(java.util.UUID.randomUUID().toString())
                .timestamp(Instant.ofEpochMilli(event.getTimeStamp()))
                .traceId(mdcMap.getOrDefault("traceId", "N/A"))
                .logLevel(event.getLevel().toString())
                .loggerName(event.getLoggerName())
                .message(event.getFormattedMessage())
                .exceptionClass(exceptionClass)
                .exceptionMessage(exceptionMessage)
                .stackTrace(stackTrace)
                .clientIp(mdcMap.getOrDefault("clientIp", "N/A"))
                .requestURI(mdcMap.getOrDefault("requestURI", "N/A"))
                .requestParams(mdcMap.getOrDefault("requestParams", "N/A"))
                .requestHeaders(mdcMap.getOrDefault("requestHeaders", "N/A"))
                .requestBody(mdcMap.getOrDefault("requestBody", "N/A"))
                .failureReason(failureReason)
                .slackFailed(slackFailed)
                .discordFailed(discordFailed)
                .retryCount(0)
                .lastRetryTime(null)
                .processed(false)
                .build();
    }

    public FailedNotificationRecord withRetryInfo(int retryCount, Instant lastRetryTime) {
        return FailedNotificationRecord.builder()
                .id(this.id)
                .timestamp(this.timestamp)
                .traceId(this.traceId)
                .logLevel(this.logLevel)
                .loggerName(this.loggerName)
                .message(this.message)
                .exceptionClass(this.exceptionClass)
                .exceptionMessage(this.exceptionMessage)
                .stackTrace(this.stackTrace)
                .clientIp(this.clientIp)
                .requestURI(this.requestURI)
                .requestParams(this.requestParams)
                .requestHeaders(this.requestHeaders)
                .requestBody(this.requestBody)
                .failureReason(this.failureReason)
                .slackFailed(this.slackFailed)
                .discordFailed(this.discordFailed)
                .retryCount(retryCount)
                .lastRetryTime(lastRetryTime)
                .processed(this.processed)
                .build();
    }

    public FailedNotificationRecord markAsProcessed() {
        return FailedNotificationRecord.builder()
                .id(this.id)
                .timestamp(this.timestamp)
                .traceId(this.traceId)
                .logLevel(this.logLevel)
                .loggerName(this.loggerName)
                .message(this.message)
                .exceptionClass(this.exceptionClass)
                .exceptionMessage(this.exceptionMessage)
                .stackTrace(this.stackTrace)
                .clientIp(this.clientIp)
                .requestURI(this.requestURI)
                .requestParams(this.requestParams)
                .requestHeaders(this.requestHeaders)
                .requestBody(this.requestBody)
                .failureReason(this.failureReason)
                .slackFailed(this.slackFailed)
                .discordFailed(this.discordFailed)
                .retryCount(this.retryCount)
                .lastRetryTime(this.lastRetryTime)
                .processed(true)
                .build();
    }

    private static String buildStackTrace(ch.qos.logback.classic.spi.IThrowableProxy throwableProxy) {
        StringBuilder sb = new StringBuilder();

        while (throwableProxy != null) {
            sb.append(throwableProxy.getClassName())
                    .append(": ")
                    .append(throwableProxy.getMessage())
                    .append("\n");

            var stackTraceElements = throwableProxy.getStackTraceElementProxyArray();
            int limit = Math.min(stackTraceElements.length, 10);

            for (int i = 0; i < limit; i++) {
                sb.append("    at ").append(stackTraceElements[i].getSTEAsString()).append("\n");
            }

            throwableProxy = throwableProxy.getCause();
            if (throwableProxy != null) {
                sb.append("Caused by: ");
            }
        }

        return sb.toString();
    }
}
