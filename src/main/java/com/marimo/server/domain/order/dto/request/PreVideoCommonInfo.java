package com.marimo.server.domain.order.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record PreVideoCommonInfo(
        @NotBlank(message = "groomName은 필수입니다.")
        @Size(min = 1, max = 20, message = "groomName은 1자 이상 20자 이하여야 합니다.")
        String groomName,

        @NotBlank(message = "brideName은 필수입니다.")
        @Size(min = 1, max = 20, message = "brideName은 1자 이상 20자 이하여야 합니다.")
        String brideName,

        @NotNull(message = "weddingDateTime은 필수입니다.")
        @Future(message = "weddingDateTime은 현재보다 이후여야 합니다.")
        @JsonFormat(pattern = "yyyy-MM-dd HH시 mm분", timezone = "Asia/Seoul")
        LocalDateTime weddingDateTime
) {

}
