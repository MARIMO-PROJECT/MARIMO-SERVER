package com.marimo.server.global.webhook;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ExecuteWebhookRequest {

    private final String username;
    private final String avatar_url;
    private final List<Embed> embeds = new ArrayList<>();

    public ExecuteWebhookRequest(String username, String avatar_url) {
        this.username = username;
        this.avatar_url = avatar_url;
    }

    public void addEmbed(Embed embed) {
        this.embeds.add(embed);
    }
}
