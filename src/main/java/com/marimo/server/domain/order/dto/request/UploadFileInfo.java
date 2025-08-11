package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UploadFileInfo(
        @NotBlank(message = "originalFileName은 공백일 수 없습니다.")
        @Size(min = 1, max = 50, message = "originalFileName은 1자 이상 50자 이하여야 합니다.")
        String originalFileName,

        @NotNull(message = "fileSizeBytes는 필수입니다.")
        @Positive(message = "fileSizeBytes는 양수여야 합니다.")
        Long fileSizeBytes
) {

}
