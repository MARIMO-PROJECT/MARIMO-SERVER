package com.marimo.server.domain.product.dto;

public record PreVideoResponse(
        Long id,
        String imageUrl,
        String sampleVideoUrl,
        String name,
        Integer discountRate,
        Integer price
) {

    public static PreVideoResponse of(
            final Long id,
            final String imageUrl,
            final String sampleVideoUrl,
            final String name,
            final Integer discountRate,
            final Integer price
    ) {
        return new PreVideoResponse(
                id,
                imageUrl,
                sampleVideoUrl,
                name,
                discountRate,
                price
        );
    }
}
