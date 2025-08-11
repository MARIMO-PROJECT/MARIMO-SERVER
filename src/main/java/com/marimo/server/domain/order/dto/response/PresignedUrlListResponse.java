package com.marimo.server.domain.order.dto.response;

import java.util.List;

public record PresignedUrlListResponse(
        List<PresignedUrl> presignedUrlList
) {

    public static PresignedUrlListResponse of(final List<PresignedUrl> presignedUrlList) {
        return new PresignedUrlListResponse(presignedUrlList);
    }
}
