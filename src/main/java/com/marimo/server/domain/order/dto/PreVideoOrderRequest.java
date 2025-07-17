package com.marimo.server.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record PreVideoOrderRequest(
        @NotNull(message = "preVideoId는 필수입니다.")
        @Positive(message = "preVideoId는 양수여야 합니다.")
        Long preVideoId,

        @NotNull(message = "customerInfo는 필수입니다.")
        @Valid CustomerInfo customerInfo,

        @NotNull(message = "preVideoCommonInfo는 필수입니다.")
        @Valid PreVideoCommonInfo preVideoCommonInfo,

        @NotEmpty(message = "mediaList에는 최소 1개 이상의 항목이 필요합니다.")
        List<@NotBlank(message = "mediaUrl은 공백일 수 없습니다.") String> mediaList,

        @NotNull(message = "hasAdditionalRequest는 필수입니다.")
        Boolean hasAdditionalRequest,

        @Valid AdditionalRequestInfo additionalRequest
) {

    private static boolean flagMatchesObj(Boolean flag, Object obj) {
        if (flag == null) {
            return false;
        }

        return flag == (obj != null);
    }

    @AssertTrue(message = "hasAdditionalRequest와 additionalRequest의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isAdditionalRequestStrict() {
        return flagMatchesObj(hasAdditionalRequest, additionalRequest);
    }
}
