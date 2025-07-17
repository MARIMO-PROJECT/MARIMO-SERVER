package com.marimo.server.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectedOption(
        @NotNull(message = "optionId는 필수입니다.")
        @Positive(message = "optionId는 양수여야 합니다.")
        Long optionId,

        @NotNull(message = "quantity는 필수입니다.")
        @Min(value = 1, message = "quantity는 1 이상이어야 합니다.")
        Integer quantity
) {

}
