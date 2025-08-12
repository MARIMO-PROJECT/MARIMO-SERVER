package com.marimo.server.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(1)
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String TIMESTAMP = "timestamp";
    private static final String CLIENT_IP = "clientIp";
    private static final String REQUEST_URI = "requestURI";
    private static final String REQUEST_PARAMS = "requestParams";
    private static final String REQUEST_HEADERS = "requestHeaders";
    private static final String REQUEST_BODY = "requestBody";
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter SEOUL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS").withZone(SEOUL_ZONE);
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest httpServletRequest,
            @NonNull final HttpServletResponse httpServletResponse,
            @NonNull final FilterChain filterChain
    ) throws IOException, ServletException {
        CachedBodyHttpServletRequest request = new CachedBodyHttpServletRequest(httpServletRequest);
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(httpServletResponse);

        try {
            MDC.put(TRACE_ID, UUID.randomUUID().toString());
            MDC.put(TIMESTAMP, SEOUL_TIME_FORMATTER.format(Instant.now()));
            MDC.put(CLIENT_IP, getClientIp(request));
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(REQUEST_PARAMS, getRequestParams(request));
            MDC.put(REQUEST_HEADERS, getRequestHeaders(request));

            // Request의 Content-Type이 JSON인 경우에만 Request Body를 로깅
            String requestBody = isJson(request.getContentType()) ? request.getBody() : "X";
            MDC.put(REQUEST_BODY, requestBody);

            String requestInfo = request.getMethod() + " | " + request.getRequestURI() + getQueryParams(request);
            String requestLog = "[Request]  " + requestInfo + " | " + getClientIp(request);

            if (!requestBody.equals("X")) {
                requestLog += " | " + requestBody;
            }

            log.info(requestLog);

            filterChain.doFilter(request, response);

            // Response의 Content-Type이 JSON인 경우에만 Response Body를 로깅
            String responseBody = isJson(response.getContentType()) ? getResponseBody(response) : "";
            String responseLog = "[Response] " + requestInfo + " | " + response.getStatus();

            if (!responseBody.isEmpty()) {
                responseLog += " | " + responseBody;
            }

            log.info(responseLog);

            // 캐싱된 Response Body를 클라이언트에게 전달하기 위해 실제 응답 스트림으로 다시 복사
            response.copyBodyToResponse();
        } finally {
            MDC.clear();
        }
    }

    private static String getQueryParams(final HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        if (parameterMap.isEmpty()) {
            return "";
        }

        return parameterMap.entrySet().stream()
                .flatMap(entry -> Arrays.stream(entry.getValue())
                        .map(value -> entry.getKey() + "=" + value))
                .collect(Collectors.joining("&", "?", ""));
    }

    // TODO: 정확한 클라이언트 IP 주소가 넘어오는지 확인
    private static String getClientIp(final HttpServletRequest request) {
        String requestAddr = request.getHeader(X_FORWARDED_FOR);

        return (requestAddr != null) ? requestAddr : request.getRemoteAddr();
    }

    private static boolean isJson(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private static String getResponseBody(ContentCachingResponseWrapper response) {
        // ContentCachingResponseWrapper에 캐싱된 Response Body를 가져와 UTF-8 문자열로 변환
        return new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private static String getRequestParams(final HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        if (parameterMap.isEmpty()) {
            return "X";
        }

        StringBuilder requestParams = new StringBuilder();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            requestParams.append(entry.getKey()).append(": ");

            String[] values = entry.getValue();

            for (int i = 0; i < values.length; i++) {
                requestParams.append(values[i]);

                if (i < values.length - 1) {
                    requestParams.append(", ");
                }
            }

            requestParams.append("\n");
        }

        return removeLastNewline(requestParams);
    }

    private static String getRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        if (!headerNames.hasMoreElements()) {
            return "X";
        }

        StringBuilder requestHeaders = new StringBuilder();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            requestHeaders.append(name).append(": ").append(value).append("\n");
        }

        return removeLastNewline(requestHeaders);
    }

    private static String removeLastNewline(StringBuilder sb) {
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}
