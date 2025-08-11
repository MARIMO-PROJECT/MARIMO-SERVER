package com.marimo.server.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PresignedUrlRequest(
        @NotBlank(message = "attachmentType은 공백일 수 없습니다.")
        String attachmentType,

        @NotEmpty(message = "uploadFileInfoList는 비어있을 수 없습니다.")
        List<@NotNull(message = "UploadFileInfo는 필수입니다.") @Valid UploadFileInfo> uploadFileInfoList
) {

}
