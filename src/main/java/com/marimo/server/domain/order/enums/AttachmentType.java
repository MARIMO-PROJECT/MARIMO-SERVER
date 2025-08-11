package com.marimo.server.domain.order.enums;

import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentType {

    PAPER_INVITATION_MAIN(EnumSet.of(FileType.JPG, FileType.JPEG), 1),
    MOBILE_INVITATION_MAIN(EnumSet.of(FileType.JPG, FileType.JPEG), 1),
    GALLERY(EnumSet.of(FileType.JPG, FileType.JPEG), 30),
    INVITATION_REQUEST(
            EnumSet.of(FileType.JPG, FileType.JPEG, FileType.PNG, FileType.MP4, FileType.MOV, FileType.PDF), 30
    ),

    PRE_VIDEO(EnumSet.of(FileType.JPG, FileType.JPEG, FileType.MP4), 30),
    PRE_VIDEO_REQUEST(
            EnumSet.of(FileType.JPG, FileType.JPEG, FileType.PNG, FileType.MP4, FileType.MOV, FileType.PDF), 30
    ),
    ;

    private final Set<FileType> allowed;

    @Getter
    private final int maxCount;

    public boolean isAllowed(FileType fileType) {
        return allowed.contains(fileType);
    }

    private static final Map<String, AttachmentType> ATTACHMENT_TYPE_MAP = new HashMap<>();

    static {
        for (AttachmentType attachmentType : AttachmentType.values()) {
            ATTACHMENT_TYPE_MAP.put(attachmentType.name(), attachmentType);
        }
    }

    public static AttachmentType fromValue(String value) {
        AttachmentType attachmentType = ATTACHMENT_TYPE_MAP.get(value.toUpperCase());

        if (attachmentType == null) {
            throw new BusinessException(ErrorType.INVALID_ATTACHMENT_TYPE_ERROR);
        }

        return attachmentType;
    }
}
