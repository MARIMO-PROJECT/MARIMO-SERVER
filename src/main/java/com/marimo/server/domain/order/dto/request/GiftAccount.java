package com.marimo.server.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GiftAccount(
        @NotEmpty(message = "groomGiftAccountList에는 최소 1개 이상의 항목이 필요합니다.")
        @Size(max = 3, message = "계좌는 최대 3개까지 입력할 수 있습니다.")
        List<@NotNull(message = "Account는 null이 될 수 없습니다.") @Valid Account> groomGiftAccountList,

        @NotEmpty(message = "brideGiftAccountList에는 최소 1개 이상의 항목이 필요합니다.")
        @Size(max = 3, message = "계좌는 최대 3개까지 입력할 수 있습니다.")
        List<@NotNull(message = "Account는 null이 될 수 없습니다.") @Valid Account> brideGiftAccountList
) {

}
