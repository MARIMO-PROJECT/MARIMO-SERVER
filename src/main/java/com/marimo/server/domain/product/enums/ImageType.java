package com.marimo.server.domain.product.enums;

import java.util.HashMap;
import java.util.Map;

public enum ImageType {

    INVITATION,
    PREVIDEO,
    INVITATION_DETAIL,
    PREVIDEO_DETAIL,
    ;

    private static final Map<String, ImageType> IMAGE_TYPE_MAP = new HashMap<>();

    static {
        for (ImageType imageType : ImageType.values()) {
            IMAGE_TYPE_MAP.put(imageType.name(), imageType);
        }
    }

    public static ImageType fromValue(String value) {
        ImageType imageType = IMAGE_TYPE_MAP.get(value.toUpperCase());

        if (imageType == null) {
            throw new IllegalArgumentException("Invalid ImageType: " + value);
        }

        return imageType;
    }
}
