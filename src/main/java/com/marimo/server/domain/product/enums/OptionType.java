package com.marimo.server.domain.product.enums;

import java.util.HashMap;
import java.util.Map;

public enum OptionType {

    QUANTITY,
    STICKER,
    ENVELOPE,
    RIBBON,
    ADDITIONAL,
    MOBILE,
    ;

    private static final Map<String, OptionType> OPTION_TYPE_MAP = new HashMap<>();

    static {
        for (OptionType optionType : OptionType.values()) {
            OPTION_TYPE_MAP.put(optionType.name(), optionType);
        }
    }

    public static OptionType fromValue(String value) {
        OptionType optionType = OPTION_TYPE_MAP.get(value.toUpperCase());

        if (optionType == null) {
            throw new IllegalArgumentException("Invalid OptionType: " + value);
        }

        return optionType;
    }
}
