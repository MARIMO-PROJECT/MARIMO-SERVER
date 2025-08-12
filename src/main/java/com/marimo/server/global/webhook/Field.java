package com.marimo.server.global.webhook;

import lombok.Getter;

@Getter
public class Field {

    private final String name;
    private final String value;
    private final Boolean inline;

    public Field(String name, String value, Boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }
}
