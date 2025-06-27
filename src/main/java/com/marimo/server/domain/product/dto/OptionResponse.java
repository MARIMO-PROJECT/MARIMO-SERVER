package com.marimo.server.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record OptionResponse(
        Long id,
        String name,
        String optionDetail,
        Integer price
) {

    public static OptionResponse of(
            final Long id,
            final String name,
            final String optionDetail,
            final Integer price
    ) {
        return new OptionResponse(id, name, optionDetail, price);
    }
}
