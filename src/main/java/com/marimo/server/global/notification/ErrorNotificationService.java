package com.marimo.server.global.notification;

import com.marimo.server.global.event.ErrorEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorNotificationService {

    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ERROR_NOTIFICATION");

    @EventListener
    @Async("errorNotificationTaskExecutor")
    public void handleErrorOccurred(ErrorEvent event) {
        try {
            Exception exception = event.getException();
            String errorId = event.getErrorId();
            
            // 요청 정보 수집
            String method = event.getRequest().getMethod();
            String uri = event.getRequest().getRequestURI();
            String queryString = event.getRequest().getQueryString();
            String userAgent = event.getRequest().getHeader("User-Agent");
            String clientIp = getClientIpAddress(event.getRequest());
            
            // URL 구성
            String fullUrl = uri;
            if (StringUtils.hasText(queryString)) {
                fullUrl += "?" + queryString;
            }

            // 요청 헤더 수집 (주요 헤더만)
            StringBuilder headers = new StringBuilder();
            Enumeration<String> headerNames = event.getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (isImportantHeader(headerName)) {
                    String headerValue = event.getRequest().getHeader(headerName);
                    headers.append("\n  ").append(headerName).append(": ").append(headerValue);
                }
            }

            // 스택 트레이스 생성
            String stackTrace = getStackTrace(exception);

            String message = String.format("🚨 서버 에러 발생 - [%s]\n" +
                            "🔍 에러 ID: %s\n" +
                            "📅 발생 시각: %s\n" +
                            "🌐 요청: %s %s\n" +
                            "🖥️ 클라이언트 IP: %s\n" +
                            "🔧 User-Agent: %s\n" +
                            "📋 에러 메시지: %s\n" +
                            "📄 헤더 정보:%s\n" +
                            "📚 스택 트레이스:\n%s",
                    exception.getClass().getSimpleName(),
                    errorId,
                    event.getOccurredAt().toString(),
                    method,
                    fullUrl,
                    clientIp,
                    userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) + "..." : "Unknown",
                    exception.getMessage() != null ? exception.getMessage() : "메시지 없음",
                    headers.toString(),
                    stackTrace
            );

            ERROR_LOGGER.error(message);
            log.info("에러 알림 전송됨 - 에러 ID: {}, 예외: {}", errorId, exception.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("에러 알림 처리 중 오류 발생", e);
        }
    }

    private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isImportantHeader(String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        return lowerHeaderName.equals("content-type") ||
               lowerHeaderName.equals("authorization") ||
               lowerHeaderName.equals("x-forwarded-for") ||
               lowerHeaderName.equals("x-real-ip") ||
               lowerHeaderName.equals("host") ||
               lowerHeaderName.equals("origin") ||
               lowerHeaderName.equals("referer");
    }

    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        
        String fullStackTrace = sw.toString();
        
        // 스택 트레이스가 너무 길면 처음 20줄만 표시
        String[] lines = fullStackTrace.split("\n");
        if (lines.length > 20) {
            StringBuilder truncated = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                truncated.append(lines[i]).append("\n");
            }
            truncated.append("... (").append(lines.length - 20).append("줄 생략)");
            return truncated.toString();
        }
        
        return fullStackTrace;
    }
}