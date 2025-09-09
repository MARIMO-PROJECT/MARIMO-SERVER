package com.marimo.server.global.event;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorEvent {
    private final Exception exception;
    private final HttpServletRequest request;
    private final LocalDateTime occurredAt;
    private final String errorId;

    public ErrorEvent(Exception exception, HttpServletRequest request) {
        this.exception = exception;
        this.request = request;
        this.occurredAt = LocalDateTime.now();
        this.errorId = generateErrorId();
    }

    private String generateErrorId() {
        return "ERR-" + System.currentTimeMillis() + "-" + 
               Integer.toHexString(this.hashCode()).substring(0, 4).toUpperCase();
    }
}