package com.marimo.server.global.webhook;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class SlackWebhookRequest {

    private final String username;
    private final String icon_url;
    private final String text;
    private final List<SlackAttachment> attachments = new ArrayList<>();

    public SlackWebhookRequest(String username, String iconUrl, String text) {
        this.username = username;
        this.icon_url = iconUrl;
        this.text = text;
    }

    public void addAttachment(SlackAttachment attachment) {
        this.attachments.add(attachment);
    }
}
