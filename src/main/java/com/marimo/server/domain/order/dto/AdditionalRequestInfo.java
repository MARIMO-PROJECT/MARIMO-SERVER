package com.marimo.server.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AdditionalRequestInfo(
        @NotBlank(message = "requestText는 공백일 수 없습니다.")
        String requestText,

        List<@NotBlank(message = "attachmentUrl은 공백일 수 없습니다.") String> attachmentList
) {

}
