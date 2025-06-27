package com.marimo.server.domain.product.dto;

import com.marimo.server.domain.product.enums.OptionType;
import java.util.List;

public record OptionGroupResponse(
        OptionType optionType,
        List<OptionResponse> optionList
) {

    public static OptionGroupResponse of(
            final OptionType optionType,
            final List<OptionResponse> optionList
    ) {
        return new OptionGroupResponse(optionType, optionList);
    }
}
