package com.marimo.server.domain.order.enums;

import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileType {

    JPG,
    JPEG,
    PNG,
    MP4,
    MOV,
    PDF,
    ;

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
