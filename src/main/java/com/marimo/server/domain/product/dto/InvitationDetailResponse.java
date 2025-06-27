package com.marimo.server.domain.product.dto;

import java.util.List;

public record InvitationDetailResponse(
        String mainImageUrl,
        String name,
        Integer discountRate,
        Integer price,
        String description,
        List<OptionGroupResponse> optionGroupList,
        List<String> detailImageList
) {

    public static InvitationDetailResponse of(
            final String mainImageUrl,
            final String name,
            final Integer discountRate,
            final Integer price,
            final String description,
            final List<OptionGroupResponse> optionGroupList,
            final List<String> detailImageList
    ) {
        return new InvitationDetailResponse(
                mainImageUrl,
                name,
                discountRate,
                price,
                description,
                optionGroupList,
                detailImageList
        );
    }
}
