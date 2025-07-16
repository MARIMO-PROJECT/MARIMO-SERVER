package com.marimo.server.domain.order.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import org.springframework.util.StringUtils;

public record ContactOption(
        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 groomFatherPhoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String groomFatherPhoneNumber,

        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 groomMotherPhoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String groomMotherPhoneNumber,

        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 groomPhoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String groomPhoneNumber,

        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 brideFatherPhoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String brideFatherPhoneNumber,

        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 brideMotherPhoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String brideMotherPhoneNumber,

        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 bridePhoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String bridePhoneNumber
) {

    @AssertTrue(message = "최소 한 개 이상의 전화번호를 입력하세요.")
    private boolean isAnyPhonePresent() {
        return StringUtils.hasText(groomFatherPhoneNumber)
                || StringUtils.hasText(groomMotherPhoneNumber)
                || StringUtils.hasText(groomPhoneNumber)
                || StringUtils.hasText(brideFatherPhoneNumber)
                || StringUtils.hasText(brideMotherPhoneNumber)
                || StringUtils.hasText(bridePhoneNumber);
    }
}
