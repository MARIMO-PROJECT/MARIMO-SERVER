package com.marimo.server.domain.product.dto;

import java.util.List;

public record PreVideoListResponse(
        List<PreVideoResponse> preVideoList
) {

    public static PreVideoListResponse of(final List<PreVideoResponse> preVideoList) {
        return new PreVideoListResponse(preVideoList);
    }
}
