package com.marimo.server.global.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.marimo.server.global.circuitbreaker.CircuitBreakerConfig;
import com.marimo.server.global.circuitbreaker.SlackCircuitBreaker;
import com.marimo.server.global.webhook.DiscordWebhookAdapter;
import com.marimo.server.global.webhook.Embed;
import com.marimo.server.global.webhook.ExecuteWebhookRequest;
import com.marimo.server.global.webhook.Field;
import com.marimo.server.global.webhook.SlackAttachment;
import com.marimo.server.global.webhook.SlackField;
import com.marimo.server.global.webhook.SlackWebhookAdapter;
import com.marimo.server.global.webhook.SlackWebhookRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class OrderNotificationAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter SEOUL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS").withZone(SEOUL_ZONE);
    private static final int MAX_EMBED_DESCRIPTION_LENGTH = 4096;
    private static final Cache<String, Boolean> loggedTraceIds = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .initialCapacity(20)
            .maximumSize(100)
            .build();

    @Setter
    private String username;

    @Setter
    private String avatarUrl;

    @Setter
    private String slackWebhookUrl;

    @Setter
    private String discordWebhookUrl;

    // Circuit Breaker 설정 (에러 알림과 동일)
    @Setter
    private int failureThreshold = 5;

    @Setter
    private int successThreshold = 3;

    @Setter
    private long timeoutDuration = 60000; // 1분

    private SlackCircuitBreaker circuitBreaker;
    private FailedNotificationAppender failedNotificationAppender;

    @Override
    public void start() {
        // Circuit Breaker 초기화
        CircuitBreakerConfig config = new CircuitBreakerConfig();
        config.setFailureThreshold(failureThreshold);
        config.setSuccessThreshold(successThreshold);
        config.setTimeoutDuration(timeoutDuration);

        this.circuitBreaker = new SlackCircuitBreaker(config);

        // Failed Notification Appender 초기화
        this.failedNotificationAppender = new FailedNotificationAppender();
        this.failedNotificationAppender.start();

        log.info("🔧 OrderNotificationAppender 시작됨 - Circuit Breaker 설정: " +
                        "실패임계값={}, 성공임계값={}, 타임아웃={}ms",
                failureThreshold, successThreshold, timeoutDuration);

        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        // INFO 레벨의 주문 관련 로그만 처리
        if (!event.getLevel().equals(Level.INFO)) {
            return;
        }

        // 주문 관련 로그인지 확인 (특정 마커나 메시지 패턴으로 필터링)
        String message = event.getFormattedMessage();
        if (!isOrderNotification(message)) {
            return;
        }

        String traceId = getMdcValue(event.getMDCPropertyMap(), "traceId");

        // Circuit Breaker 상태 확인
        if (circuitBreaker.canExecute()) {
            // Slack 우선 시도
            sendSlackNotificationWithFallback(event, traceId);
        } else {
            // Circuit Breaker가 OPEN 상태면 바로 Discord로 fallback
            log.debug("🔴 Circuit Breaker OPEN - Discord fallback 사용 (주문 알림): {}", traceId);
            sendDiscordNotificationWithFailureTracking(event, traceId, true, true, false);
        }
    }

    private boolean isOrderNotification(String message) {
        // 주문 성공 알림을 구분하는 패턴
        return message != null && (
                message.contains("[주문 성공]") ||
                        message.contains("[ORDER_SUCCESS]") ||
                        message.startsWith("🎉 새로운 주문이 접수되었습니다")
        );
    }

    private void sendSlackNotificationWithFallback(ILoggingEvent event, String traceId) {
        if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
            log.warn("Slack 웹훅 URL이 설정되지 않음 - Discord로 fallback (주문 알림)");
            sendDiscordNotificationWithFailureTracking(event, traceId, true, true, false);
            return;
        }

        SlackWebhookAdapter slackWebhookAdapter = new SlackWebhookAdapter(slackWebhookUrl);
        SlackWebhookRequest slackRequest = createSlackRequest(event);

        Callback slackCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Slack 실패 시 Circuit Breaker에 실패 기록
                circuitBreaker.recordFailure();

                logWebhookFailure("Slack (주문 알림)", traceId, e);

                // Discord로 fallback 시도
                log.warn("🔴 Slack 주문 알림 실패 - Discord로 fallback: {}", traceId);
                sendDiscordNotificationWithFailureTracking(event, traceId, true, true, false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    if (response.isSuccessful()) {
                        // Slack 성공 시 Circuit Breaker에 성공 기록
                        circuitBreaker.recordSuccess();
                        log.debug("✅ Slack 주문 알림 성공: {}", traceId);
                    } else {
                        // HTTP 에러 응답도 실패로 처리
                        circuitBreaker.recordFailure();
                        log.error("🔴 Slack 주문 알림 HTTP 에러 ({}), Discord로 fallback: {}", response.code(), traceId);
                        sendDiscordNotificationWithFailureTracking(event, traceId, true, true, false);
                    }
                } catch (Exception e) {
                    circuitBreaker.recordFailure();
                    log.error("🔴 Slack 주문 알림 응답 처리 중 에러, Discord로 fallback: {}", traceId, e);
                    sendDiscordNotificationWithFailureTracking(event, traceId, true, true, false);
                }
            }
        };

        slackWebhookAdapter.execute(slackRequest, slackCallback);
    }

    private void sendDiscordNotificationWithFailureTracking(
            ILoggingEvent event,
            String traceId,
            boolean isFallback,
            boolean slackFailed,
            boolean discordFailed
    ) {
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
            log.error("Discord 웹훅 URL이 설정되지 않음 - 양쪽 플랫폼 모두 실패 (주문 알림): {}", traceId);

            // 양쪽 모두 실패했으므로 실패 알림 로그에 저장
            failedNotificationAppender.logFailedNotification(event, "ORDER_NOTIFICATION_BOTH_FAILED", true, true);
            return;
        }

        DiscordWebhookAdapter discordWebhookAdapter = new DiscordWebhookAdapter(discordWebhookUrl);
        ExecuteWebhookRequest discordRequest = createDiscordRequest(event, isFallback);

        Callback discordCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logWebhookFailure("Discord (주문 알림)" + (isFallback ? " (Fallback)" : ""), traceId, e);

                // Discord도 실패했으므로 실패 알림 로그에 저장
                String failureReason =
                        slackFailed ? "ORDER_NOTIFICATION_BOTH_FAILED" : "ORDER_NOTIFICATION_DISCORD_FAILED";
                failedNotificationAppender.logFailedNotification(
                        event,
                        failureReason,
                        slackFailed,
                        true
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    if (response.isSuccessful()) {
                        log.debug("✅ Discord 주문 알림 성공{}: {}", isFallback ? " (Fallback)" : "", traceId);
                    } else {
                        log.error("🔴 Discord 주문 알림 HTTP 에러 ({}){}: {}", response.code(),
                                isFallback ? " (Fallback)" : "", traceId);

                        // HTTP 에러도 실패로 처리
                        String failureReason =
                                slackFailed ? "ORDER_NOTIFICATION_BOTH_FAILED" : "ORDER_NOTIFICATION_DISCORD_FAILED";
                        failedNotificationAppender.logFailedNotification(
                                event,
                                failureReason,
                                slackFailed,
                                true
                        );
                    }
                } catch (Exception e) {
                    log.error("🔴 Discord 주문 알림 응답 처리 중 에러{}: {}", isFallback ? " (Fallback)" : "", traceId, e);

                    // 응답 처리 에러도 실패로 처리
                    String failureReason =
                            slackFailed ? "ORDER_NOTIFICATION_BOTH_FAILED" : "ORDER_NOTIFICATION_DISCORD_FAILED";
                    failedNotificationAppender.logFailedNotification(
                            event,
                            failureReason,
                            slackFailed,
                            true
                    );
                }
            }
        };

        discordWebhookAdapter.execute(discordRequest, discordCallback);
    }

    private SlackWebhookRequest createSlackRequest(ILoggingEvent event) {
        String mainText = "🎉 *MARIMO 새로운 주문 접수*";

        SlackWebhookRequest slackRequest = new SlackWebhookRequest(username, avatarUrl, mainText);

        String slackColor = "good"; // 성공 알림은 녹색
        String timestamp = String.valueOf(event.getTimeStamp() / 1000);
        String title = "새로운 주문이 들어왔습니다!";
        String orderMessage = event.getFormattedMessage();

        SlackAttachment attachment = new SlackAttachment(slackColor, title, orderMessage, timestamp);

        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        String traceId = getMdcValue(mdcPropertyMap, "traceId");

        attachment.addField(new SlackField("주문 접수 시각", formatToSeoulTime(event.getTimeStamp()), true));
        attachment.addField(new SlackField("Trace ID", traceId, true));
        attachment.addField(new SlackField("고객 IP", getMdcValue(mdcPropertyMap, "clientIp"), true));
        attachment.addField(new SlackField("요청 URI", getMdcValue(mdcPropertyMap, "requestURI"), false));
        attachment.addField(new SlackField("요청 Parameters", getMdcValue(mdcPropertyMap, "requestParams"), false));
        attachment.addField(new SlackField("요청 Body", getMdcValue(mdcPropertyMap, "requestBody"), false));

        slackRequest.addAttachment(attachment);

        return slackRequest;
    }

    private ExecuteWebhookRequest createDiscordRequest(ILoggingEvent event, boolean isFallback) {
        ExecuteWebhookRequest executeWebhookRequest = new ExecuteWebhookRequest(username, avatarUrl);

        String title = "새로운 주문이 들어왔습니다!";
        if (isFallback) {
            title += " 🔄 [Slack Fallback]";
        }

        String orderMessage = event.getFormattedMessage();

        int embedColor = 0x00FF00; // 녹색 (성공 알림)
        String timestamp = formatToSeoulTime(event.getTimeStamp());
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        String traceId = getMdcValue(mdcPropertyMap, "traceId");

        Embed embed = new Embed(title, orderMessage, embedColor, null);

        embed.addField(new Field("[주문 접수 시각]", timestamp, false));
        embed.addField(new Field("[Trace ID]", traceId, false));
        if (isFallback) {
            embed.addField(new Field("[알림 방식]", "🔄 Slack → Discord Fallback", false));
        }
        embed.addField(new Field("[고객 IP 정보]", getMdcValue(mdcPropertyMap, "clientIp"), false));
        embed.addField(new Field("[요청 URI 정보]", getMdcValue(mdcPropertyMap, "requestURI"), false));
        embed.addField(new Field("[요청 Parameter 정보]", getMdcValue(mdcPropertyMap, "requestParams"), false));
        embed.addField(new Field("[요청 Header 정보]", getMdcValue(mdcPropertyMap, "requestHeaders"), false));
        embed.addField(new Field("[요청 Body 정보]", getMdcValue(mdcPropertyMap, "requestBody"), false));

        executeWebhookRequest.addEmbed(embed);

        return executeWebhookRequest;
    }

    private void logWebhookFailure(String platform, String traceId, IOException e) {
        if (!traceId.equals("N/A")) {
            if (loggedTraceIds.getIfPresent(traceId) == null) {
                loggedTraceIds.put(traceId, true);
                log.error("{} 웹훅 실행 실패: {}", platform, traceId, e);
            } else {
                log.warn("{} 웹훅 실행 실패 (중복): {}", platform, traceId);
            }
        } else {
            log.warn("{} 웹훅 실행 실패", platform, e);
        }
    }

    // Circuit Breaker 상태 조회 메서드 (관리용)
    public SlackCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    private static String formatToSeoulTime(long timestamp) {
        ZonedDateTime seoulTime = Instant.ofEpochMilli(timestamp).atZone(SEOUL_ZONE);
        return SEOUL_TIME_FORMATTER.format(seoulTime);
    }

    private static String getMdcValue(Map<String, String> mdcPropertyMap, String key) {
        return mdcPropertyMap.getOrDefault(key, "N/A");
    }
}
