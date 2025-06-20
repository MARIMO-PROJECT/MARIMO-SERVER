package com.marimo.server.domain.product.dto;

public record BannerResponse(
        String fileUrl,
        String ctaUrl
) {

    public static BannerResponse of(
            final String fileUrl,
            final String ctaUrl
    ) {
        return new BannerResponse(fileUrl, ctaUrl);
    }
}
