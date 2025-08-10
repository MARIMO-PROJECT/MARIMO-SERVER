package com.marimo.server.domain.order.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record Reception(
        @NotBlank(message = "address는 공백일 수 없습니다.")
        @Size(max = 100, message = "address는 최대 100자여야 합니다.")
        String address,

        @NotNull(message = "dateTime은 필수입니다.")
        @Future(message = "dateTime은 현재보다 이후여야 합니다.")
        @JsonFormat(pattern = "yyyy-MM-dd HH시 mm분", timezone = "Asia/Seoul")
        LocalDateTime dateTime
) {

}
