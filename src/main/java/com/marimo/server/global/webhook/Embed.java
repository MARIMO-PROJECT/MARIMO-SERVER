package com.marimo.server.global.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class Embed {

    private final String title;
    private final String description;
    private final Integer color;
    private final String timestamp;
    private final List<Field> fields = new ArrayList<>();

    public Embed(String title, String description, Integer color, String timestamp) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.timestamp = timestamp;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }
}
