package com.marimo.server.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public record CharterBus(
        @NotBlank(message = "busStopLocation은 공백일 수 없습니다.")
        @Size(max = 100, message = "busStopLocation은 최대 100자여야 합니다.")
        String busStopLocation,

        @NotEmpty(message = "busStopTimeList에는 최소 1개 이상의 항목이 필요합니다.")
        @Size(max = 10, message = "busStopTime은 최대 10개입니다.")
        @JsonFormat(pattern = "HH시 mm분", timezone = "Asia/Seoul")
        List<@NotNull(message = "busStopTime은 필수입니다.") LocalTime> busStopTimeList
) {

}
