package com.marimo.server.global.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class SlackAttachment {

    private final String color;
    private final String title;
    private final String text;
    private final String timestamp;
    private final List<SlackField> fields = new ArrayList<>();

    public SlackAttachment(String color, String title, String text, String timestamp) {
        this.color = color;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
    }

    public void addField(SlackField field) {
        this.fields.add(field);
    }
}
