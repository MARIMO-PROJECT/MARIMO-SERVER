package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdditionalRequestInfo(
        @NotBlank(message = "requestText는 공백일 수 없습니다.")
        @Size(min = 1, max = 500, message = "requestText는 1자 이상 500자 이하여야 합니다.")
        String requestText,

        List<@NotBlank(message = "attachmentUrl은 공백일 수 없습니다.") String> attachmentList
) {

}
