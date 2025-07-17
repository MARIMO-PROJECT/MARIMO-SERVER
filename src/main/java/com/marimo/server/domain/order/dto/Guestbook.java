package com.marimo.server.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Guestbook(
        @NotBlank(message = "adminPassword는 공백일 수 없습니다.")
        @Pattern(regexp = "\\d{4}", message = "adminPassword는 4자리 숫자여야 합니다.")
        String adminPassword
) {

}
