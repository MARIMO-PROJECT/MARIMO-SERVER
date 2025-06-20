package com.marimo.server.domain.product.dto;

import java.util.List;

public record BannerListResponse(
        List<BannerResponse> bannerList
) {

    public static BannerListResponse of(final List<BannerResponse> bannerList) {
        return new BannerListResponse(bannerList);
    }
}
