package com.marimo.server.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
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
