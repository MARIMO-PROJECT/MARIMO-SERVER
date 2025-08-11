package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerInfo(
        @NotBlank(message = "name은 공백일 수 없습니다.")
        @Size(min = 2, max = 20, message = "name은 2자 이상 20자 이하여야 합니다.")
        String name,

        @NotBlank(message = "zoneCode는 공백일 수 없습니다.")
        @Pattern(regexp = "\\d{5}", message = "zoneCode는 5자리 숫자여야 합니다.")
        String zoneCode,

        @NotBlank(message = "address는 공백일 수 없습니다.")
        @Size(max = 100, message = "address는 최대 100자입니다.")
        String address,

        @Size(max = 100, message = "detailAddress는 최대 100자입니다.")
        String detailAddress,

        @NotBlank(message = "phoneNumber는 공백일 수 없습니다.")
        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{4}-\\d{4}$", message = "유효한 phoneNumber 형식이 아닙니다. (예: 010-1234-5678)")
        String phoneNumber,

        @NotBlank(message = "email은 공백일 수 없습니다.")
        @Size(max = 100, message = "email은 최대 100자입니다.")
        @Email(message = "유효한 email 형식이 아닙니다.")
        String email
) {

}
