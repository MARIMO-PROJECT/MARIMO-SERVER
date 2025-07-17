package com.marimo.server.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MobileInvitationInfo(
        @NotBlank(message = "urlSlug는 공백일 수 없습니다.")
        @Pattern(regexp = "^[a-z0-9]{3,15}$", message = "urlSlug는 영문 소문자와 숫자만 사용할 수 있으며 3~15자 사이여야 합니다.")
        String urlSlug,

        @NotBlank(message = "mainImage는 공백일 수 없습니다.")
        String mainImage,

        @NotBlank(message = "message는 공백일 수 없습니다.")
        @Size(min = 1, max = 150, message = "message는 1자 이상 150자 이하여야 합니다.")
        String message
) {

}
