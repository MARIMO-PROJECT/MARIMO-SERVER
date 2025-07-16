package com.marimo.server.domain.order.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentType {

    PAPER_INVITATION_MAIN,
    MOBILE_INVITATION_MAIN,
    GALLERY,
    PREVIDEO_IMAGE,
    PREVIDEO_VIDEO,
    INVITATION_REQUEST,
    PREVIDEO_REQUEST,
    ;

    private static final Map<String, AttachmentType> ATTACHMENT_TYPE_MAP = new HashMap<>();

    static {
        for (AttachmentType attachmentType : AttachmentType.values()) {
            ATTACHMENT_TYPE_MAP.put(attachmentType.name(), attachmentType);
        }
    }

    public static AttachmentType fromValue(String value) {
        AttachmentType attachmentType = ATTACHMENT_TYPE_MAP.get(value.toUpperCase());

        if (attachmentType == null) {
            throw new IllegalArgumentException("Invalid AttachmentType: " + value);
        }

        return attachmentType;
    }
}
