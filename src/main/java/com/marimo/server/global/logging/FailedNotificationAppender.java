package com.marimo.server.global.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marimo.server.global.notification.FailedNotificationRecord;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FailedNotificationAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Setter
    private String logPath = "./logs/failed-notifications"; // 기본 경로

    @Override
    public void start() {
        // 로그 디렉토리 생성
        try {
            Path path = Paths.get(logPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("📁 실패 알림 로그 디렉토리 생성: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            addError("실패 알림 로그 디렉토리 생성 실패: " + logPath, e);
        }

        super.start();
    }

    public void logFailedNotification(
            ILoggingEvent event,
            String failureReason,
            boolean slackFailed,
            boolean discordFailed
    ) {
        try {
            FailedNotificationRecord record = FailedNotificationRecord.fromLoggingEvent(
                    event, failureReason, slackFailed, discordFailed
            );

            writeToFile(record);

            log.warn("💾 실패 알림 저장됨 - ID: {}, TraceID: {}, 사유: {}",
                    record.getId(), record.getTraceId(), record.getFailureReason());

        } catch (Exception e) {
            addError("실패 알림 로그 저장 중 에러 발생", e);
        }
    }

    private void writeToFile(FailedNotificationRecord record) throws IOException {
        String fileName = generateFileName();
        File file = new File(logPath, fileName);

        try (FileWriter writer = new FileWriter(file, true)) {
            String json = OBJECT_MAPPER.writeValueAsString(record);
            writer.write(json + System.lineSeparator());
        }
    }

    private String generateFileName() {
        // 날짜별로 파일 분리: failed-notifications-2025-01-15.json
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "failed-notifications-" + dateStr + ".json";
    }

    @Override
    protected void append(ILoggingEvent event) {
        // 이 appender는 직접 호출되지 않고, logFailedNotification 메서드를 통해 사용됨
    }
}
