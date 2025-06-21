package com.marimo.server.domain.product.dto;

public record InvitationResponse(
        Long id,
        String imageUrl,
        Boolean hasBundle,
        String name,
        Integer discountRate,
        Integer price,
        String quantity
) {

    public static InvitationResponse of(
            final Long id,
            final String imageUrl,
            final Boolean hasBundle,
            final String name,
            final Integer discountRate,
            final Integer price,
            final String quantity
    ) {
        return new InvitationResponse(id, imageUrl, hasBundle, name, discountRate, price, quantity);
    }
}
