package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaperInvitationInfo(
        @NotBlank(message = "mainImage는 공백일 수 없습니다.")
        String mainImage,

        @NotBlank(message = "message는 공백일 수 없습니다.")
        @Size(min = 1, max = 150, message = "message는 1자 이상 150자 이하여야 합니다.")
        String message
) {

}
