package com.marimo.server.domain.order.dto.response;

public record PresignedUrl(
        String fileName,
        String presignedUrl
) {

    public static PresignedUrl of(
            final String fileName,
            final String presignedUrl
    ) {
        return new PresignedUrl(fileName, presignedUrl);
    }
}
