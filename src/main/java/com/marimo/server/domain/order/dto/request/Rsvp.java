package com.marimo.server.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;

public record Rsvp(
        @NotNull(message = "hasPrimaryContactField는 필수입니다.")
        Boolean hasPrimaryContactField,

        @NotNull(message = "hasCompanionField는 필수입니다.")
        Boolean hasCompanionField,

        @NotNull(message = "hasMealOptionField는 필수입니다.")
        Boolean hasMealOptionField
) {

}
