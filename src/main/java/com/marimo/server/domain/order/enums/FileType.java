package com.marimo.server.domain.order.enums;

import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileType {

    JPG("image/jpeg", 20),
    JPEG("image/jpeg", 20),
    PNG("image/png", 20),
    MP4("video/mp4", 500),
    MOV("video/quicktime", 500),
    PDF("application/pdf", 50),
    ;

    @Getter
    private final String mimeType;

    private final int maxSizeMb;

    public long maxSizeBytes() {
        return maxSizeMb * 1_024L * 1_024L;
    }

    private static final Map<String, FileType> FILE_TYPE_MAP = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            FILE_TYPE_MAP.put(fileType.name(), fileType);
        }
    }

    public static FileType fromValue(String value) {
        FileType fileType = FILE_TYPE_MAP.get(value.toUpperCase());

        if (fileType == null) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }

        return fileType;
    }
}
