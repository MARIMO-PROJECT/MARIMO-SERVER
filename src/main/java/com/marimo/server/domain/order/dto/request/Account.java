package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Account(
        @NotBlank(message = "bank는 공백일 수 없습니다.")
        @Size(min = 1, max = 20, message = "bank는 1자 이상 20자 이하여야 합니다.")
        String bank,

        @NotBlank(message = "holder는 공백일 수 없습니다.")
        @Size(min = 2, max = 20, message = "holder는 2자 이상 20자 이하여야 합니다.")
        String holder,

        @NotBlank(message = "number는 공백일 수 없습니다.")
        @Size(min = 1, max = 20, message = "number는 1자 이상 20자 이하여야 합니다.")
        String number
) {

}
