package com.marimo.server.global.webhook;

import lombok.Getter;

@Getter
public class SlackField {

    private final String title;
    private final String value;
    private final Boolean short_field;  // Slack은 'short' 필드명 사용

    public SlackField(String title, String value, Boolean isShort) {
        this.title = title;
        this.value = value;
        this.short_field = isShort;
    }
}
