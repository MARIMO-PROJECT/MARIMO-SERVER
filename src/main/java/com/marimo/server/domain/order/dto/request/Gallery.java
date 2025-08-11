package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record Gallery(
        @NotEmpty(message = "imageList에는 최소 1개 이상의 항목이 필요합니다.")
        List<@NotBlank(message = "imageUrl은 공백일 수 없습니다.") String> imageList
) {

}
