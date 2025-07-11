package com.marimo.server.domain.product.dto;

public record PreVideoDetailResponse(
        String mainImageUrl,
        String name,
        Integer discountRate,
        Integer price,
        String description,
        String sampleVideoUrl
) {

    public static PreVideoDetailResponse of(
            final String mainImageUrl,
            final String name,
            final Integer discountRate,
            final Integer price,
            final String description,
            final String sampleVideoUrl
    ) {
        return new PreVideoDetailResponse(
                mainImageUrl,
                name,
                discountRate,
                price,
                description,
                sampleVideoUrl
        );
    }
}
