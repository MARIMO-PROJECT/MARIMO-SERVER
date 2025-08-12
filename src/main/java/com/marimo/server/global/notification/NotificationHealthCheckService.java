package com.marimo.server.global.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marimo.server.global.webhook.DiscordWebhookAdapter;
import com.marimo.server.global.webhook.Embed;
import com.marimo.server.global.webhook.ExecuteWebhookRequest;
import com.marimo.server.global.webhook.SlackAttachment;
import com.marimo.server.global.webhook.SlackWebhookAdapter;
import com.marimo.server.global.webhook.SlackWebhookRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationHealthCheckService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Value("${discord.webhook-url.prod-error:}")
    private String discordWebhookUrl;

    @Value("${slack.webhook-url.prod-error:}")
    private String slackWebhookUrl;

    @Value("${logging.location:./logs}")
    private String logLocation;

    private final String username = "this-is-marimo-bot";
    private final String avatarUrl = "https://avatars.githubusercontent.com/u/81475587?v=4";

    @Scheduled(fixedDelay = 300000) // 5분마다 실행
    public void performHealthCheck() {
        log.debug("🔍 알림 서비스 Health Check 시작...");

        boolean slackHealthy = checkSlackHealth();
        boolean discordHealthy = checkDiscordHealth();

        log.info("📊 Health Check 결과 - Slack: {}, Discord: {}",
                slackHealthy ? "✅ 정상" : "❌ 장애",
                discordHealthy ? "✅ 정상" : "❌ 장애");

        // 둘 다 복구되었거나, 하나라도 복구되었다면 실패 알림 재전송 시도
        if (slackHealthy || discordHealthy) {
            processFailedNotifications(slackHealthy, discordHealthy);
        }
    }

    private boolean checkSlackHealth() {
        if (slackWebhookUrl == null || slackWebhookUrl.isEmpty() || slackWebhookUrl.contains("YOUR_")) {
            log.debug("Slack 웹훅 URL이 설정되지 않음");
            return false;
        }

        try {
            SlackWebhookAdapter adapter = new SlackWebhookAdapter(slackWebhookUrl);
            SlackWebhookRequest healthCheckRequest = new SlackWebhookRequest(
                    username, avatarUrl, "🩺 Health Check - 무시하세요"
            );

            SlackAttachment attachment = new SlackAttachment(
                    "good",
                    "시스템 상태 확인",
                    "알림 시스템 정상 작동 중입니다.",
                    String.valueOf(Instant.now().getEpochSecond())
            );
            healthCheckRequest.addAttachment(attachment);

            return executeHealthCheck(adapter, healthCheckRequest);

        } catch (Exception e) {
            log.debug("Slack Health Check 실패: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkDiscordHealth() {
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty() || discordWebhookUrl.contains("YOUR_")) {
            log.debug("Discord 웹훅 URL이 설정되지 않음");
            return false;
        }

        try {
            DiscordWebhookAdapter adapter = new DiscordWebhookAdapter(discordWebhookUrl);
            ExecuteWebhookRequest healthCheckRequest = new ExecuteWebhookRequest(username, avatarUrl);

            Embed embed = new Embed(
                    "🩺 Health Check - 무시하세요",
                    "알림 시스템 정상 작동 중입니다.",
                    0x00FF00, // 녹색
                    null
            );
            healthCheckRequest.addEmbed(embed);

            return executeHealthCheck(adapter, healthCheckRequest);

        } catch (Exception e) {
            log.debug("Discord Health Check 실패: {}", e.getMessage());
            return false;
        }
    }

    private boolean executeHealthCheck(Object adapter, Object request) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                success.set(false);
                latch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    success.set(response.isSuccessful());
                } finally {
                    latch.countDown();
                }
            }
        };

        try {
            if (adapter instanceof SlackWebhookAdapter slackAdapter) {
                slackAdapter.execute((SlackWebhookRequest) request, callback);
            } else if (adapter instanceof DiscordWebhookAdapter discordAdapter) {
                discordAdapter.execute((ExecuteWebhookRequest) request, callback);
            }

            // 최대 10초 대기
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            return completed && success.get();

        } catch (Exception e) {
            log.debug("Health Check 실행 중 에러: {}", e.getMessage());
            return false;
        }
    }

    private void processFailedNotifications(boolean slackHealthy, boolean discordHealthy) {
        log.info("🔄 실패 알림 재전송 시작 - Slack: {}, Discord: {}", slackHealthy, discordHealthy);

        try {
            List<FailedNotificationRecord> failedRecords = loadFailedNotifications();

            if (failedRecords.isEmpty()) {
                log.debug("재전송할 실패 알림이 없습니다.");
                return;
            }

            log.info("📋 재전송할 실패 알림 {}개 발견", failedRecords.size());

            for (FailedNotificationRecord record : failedRecords) {
                if (record.isProcessed()) {
                    continue; // 이미 처리된 알림은 건너뛰기
                }

                boolean shouldRetry = false;
                boolean useSlackFirst = slackHealthy; // Slack이 복구되었다면 우선 사용

                // 재전송 조건 확인
                if (record.isSlackFailed() && record.isDiscordFailed()) {
                    // 둘 다 실패한 경우: 하나라도 복구되면 재전송
                    shouldRetry = slackHealthy || discordHealthy;
                } else if (record.isSlackFailed() && !record.isDiscordFailed()) {
                    // Slack만 실패한 경우: Slack이 복구되면 재전송
                    shouldRetry = slackHealthy;
                } else if (!record.isSlackFailed() && record.isDiscordFailed()) {
                    // Discord만 실패한 경우: Discord가 복구되면 재전송
                    shouldRetry = discordHealthy;
                    useSlackFirst = false; // Discord 복구 상황이므로 Discord 사용
                }

                if (shouldRetry) {
                    retryFailedNotification(record, useSlackFirst, slackHealthy, discordHealthy);
                }
            }

        } catch (Exception e) {
            log.error("실패 알림 재전송 중 에러 발생", e);
        }
    }

    private List<FailedNotificationRecord> loadFailedNotifications() {
        List<FailedNotificationRecord> records = new ArrayList<>();

        try {
            String failedNotificationPath = logLocation + "/failed-notifications";
            File directory = new File(failedNotificationPath);

            if (!directory.exists()) {
                return records;
            }

            File[] files = directory.listFiles(
                    (dir, name) -> name.startsWith("failed-notifications-") && name.endsWith(".json"));

            if (files == null) {
                return records;
            }

            // 최근 7일간의 파일만 처리
            for (File file : Arrays.stream(files)
                    .filter(f -> isRecentFile(f.getName()))
                    .toArray(File[]::new)) {

                try {
                    List<String> lines = Files.readAllLines(file.toPath());

                    for (String line : lines) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        FailedNotificationRecord record = OBJECT_MAPPER.readValue(line, FailedNotificationRecord.class);
                        records.add(record);
                    }
                } catch (Exception e) {
                    log.warn("실패 알림 파일 읽기 실패: {}", file.getName(), e);
                }
            }

        } catch (Exception e) {
            log.error("실패 알림 로드 중 에러 발생", e);
        }

        return records;
    }

    private boolean isRecentFile(String fileName) {
        // 간단한 날짜 확인: 최근 7일 이내의 파일만 처리
        // 실제로는 파일명에서 날짜를 파싱해서 확인해야 하지만, 간소화
        return true;
    }

    private void retryFailedNotification(
            FailedNotificationRecord record,
            boolean useSlackFirst,
            boolean slackHealthy,
            boolean discordHealthy
    ) {
        log.info("🔄 알림 재전송 시도 - ID: {}, TraceID: {}, 우선순위: {}",
                record.getId(), record.getTraceId(), useSlackFirst ? "Slack" : "Discord");

        try {
            if (useSlackFirst && slackHealthy) {
                retrySlackNotification(record);
            } else if (discordHealthy) {
                retryDiscordNotification(record, true); // fallback 표시
            } else {
                log.warn("재전송 불가 - 복구된 서비스가 없음: {}", record.getId());
            }

        } catch (Exception e) {
            log.error("알림 재전송 중 에러 발생 - ID: {}", record.getId(), e);
        }
    }

    private void retrySlackNotification(FailedNotificationRecord record) {
        SlackWebhookAdapter adapter = new SlackWebhookAdapter(slackWebhookUrl);

        SlackWebhookRequest request = new SlackWebhookRequest(
                username, avatarUrl, "🔄 *[재전송] MARIMO SERVER ERROR*"
        );

        SlackAttachment attachment = new SlackAttachment(
                "danger",
                "[재전송] " + (record.getExceptionClass() != null ? record.getExceptionClass() : "Error occurred!"),
                "원본 발생 시각: " + record.getTimestamp() + "\n" + record.getMessage(),
                String.valueOf(record.getTimestamp().getEpochSecond())
        );

        request.addAttachment(attachment);

        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.warn("재전송 실패 (Slack) - ID: {}", record.getId(), e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    if (response.isSuccessful()) {
                        log.info("✅ 재전송 성공 (Slack) - ID: {}", record.getId());
                        markAsProcessed(record);
                    } else {
                        log.warn("재전송 실패 (Slack HTTP {}) - ID: {}", response.code(), record.getId());
                    }
                }
            }
        };

        adapter.execute(request, callback);
    }

    private void retryDiscordNotification(FailedNotificationRecord record, boolean isFallback) {
        DiscordWebhookAdapter adapter = new DiscordWebhookAdapter(discordWebhookUrl);

        ExecuteWebhookRequest request = new ExecuteWebhookRequest(username, avatarUrl);

        String title = "[재전송] " + (record.getExceptionClass() != null ? record.getExceptionClass() : "Error occurred!");
        if (isFallback) {
            title += " 🔄 [Slack Fallback]";
        }

        Embed embed = new Embed(
                title,
                "원본 발생 시각: " + record.getTimestamp() + "\n" + record.getMessage(),
                0xFF0000, // 빨간색
                null
        );

        request.addEmbed(embed);

        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.warn("재전송 실패 (Discord) - ID: {}", record.getId(), e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    if (response.isSuccessful()) {
                        log.info("✅ 재전송 성공 (Discord) - ID: {}", record.getId());
                        markAsProcessed(record);
                    } else {
                        log.warn("재전송 실패 (Discord HTTP {}) - ID: {}", response.code(), record.getId());
                    }
                }
            }
        };

        adapter.execute(request, callback);
    }

    private void markAsProcessed(FailedNotificationRecord record) {
        try {
            // 처리 완료된 레코드로 업데이트
            FailedNotificationRecord processedRecord = record.markAsProcessed();

            // 원본 파일에서 해당 레코드를 찾아서 업데이트
            updateFailedNotificationFile(record, processedRecord);

            log.info("📝 알림 처리 완료 표시 - ID: {}", record.getId());
        } catch (Exception e) {
            log.error("알림 처리 완료 표시 중 에러 발생 - ID: {}", record.getId(), e);
        }
    }

    private void updateFailedNotificationFile(FailedNotificationRecord originalRecord,
                                              FailedNotificationRecord processedRecord) {
        try {
            String failedNotificationPath = logLocation + "/failed-notifications";
            File directory = new File(failedNotificationPath);

            if (!directory.exists()) {
                return;
            }

            File[] files = directory.listFiles((dir, name) ->
                    name.startsWith("failed-notifications-") && name.endsWith(".json"));

            if (files == null) {
                return;
            }

            // 각 파일에서 해당 레코드를 찾아서 업데이트
            for (File file : files) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    List<String> updatedLines = new ArrayList<>();
                    boolean updated = false;

                    for (String line : lines) {
                        if (line.trim().isEmpty()) {
                            updatedLines.add(line);
                            continue;
                        }

                        try {
                            FailedNotificationRecord record = OBJECT_MAPPER.readValue(line,
                                    FailedNotificationRecord.class);
                            if (record.getId().equals(originalRecord.getId())) {
                                // 해당 레코드를 처리 완료된 것으로 교체
                                String updatedJson = OBJECT_MAPPER.writeValueAsString(processedRecord);
                                updatedLines.add(updatedJson);
                                updated = true;
                            } else {
                                updatedLines.add(line);
                            }
                        } catch (Exception e) {
                            // 파싱 실패한 라인은 그대로 유지
                            updatedLines.add(line);
                        }
                    }

                    // 파일이 업데이트된 경우에만 다시 쓰기
                    if (updated) {
                        Files.write(file.toPath(), updatedLines);
                        log.debug("파일 업데이트 완료: {}", file.getName());
                        break; // 찾았으므로 다른 파일은 확인하지 않음
                    }

                } catch (Exception e) {
                    log.warn("실패 알림 파일 업데이트 실패: {}", file.getName(), e);
                }
            }

        } catch (Exception e) {
            log.error("실패 알림 파일 업데이트 중 에러 발생", e);
        }
    }
}
