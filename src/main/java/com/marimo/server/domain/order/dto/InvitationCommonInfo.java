package com.marimo.server.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record InvitationCommonInfo(
        /* ---------- 신랑 아버님 ---------- */
        @NotNull(message = "groomFatherDeceased는 필수입니다.")
        Boolean groomFatherDeceased,

        @NotNull(message = "hasGroomFatherChristianName은 필수입니다.")
        Boolean hasGroomFatherChristianName,

        @Size(min = 2, max = 20, message = "groomFatherName은 2자 이상 20자 이하여야 합니다.")
        String groomFatherName,

        @Size(min = 2, max = 20, message = "groomFatherChristianName은 2자 이상 20자 이하여야 합니다.")
        String groomFatherChristianName,

        /* ---------- 신랑 어머님 ---------- */
        @NotNull(message = "groomMotherDeceased는 필수입니다.")
        Boolean groomMotherDeceased,

        @NotNull(message = "hasGroomMotherChristianName은 필수입니다.")
        Boolean hasGroomMotherChristianName,

        @Size(min = 2, max = 20, message = "groomMotherName은 2자 이상 20자 이하여야 합니다.")
        String groomMotherName,

        @Size(min = 2, max = 20, message = "groomMotherChristianName은 2자 이상 20자 이하여야 합니다.")
        String groomMotherChristianName,

        /* ---------- 신랑 ---------- */
        @NotNull(message = "hasGroomChristianName은 필수입니다.")
        Boolean hasGroomChristianName,

        @NotBlank(message = "groomName은 필수입니다.")
        @Size(min = 1, max = 20, message = "groomName은 1자 이상 20자 이하여야 합니다.")
        String groomName,

        @Size(min = 2, max = 20, message = "groomChristianName은 2자 이상 20자 이하여야 합니다.")
        String groomChristianName,

        /* ---------- 신부 아버님 ---------- */
        @NotNull(message = "brideFatherDeceased는 필수입니다.")
        Boolean brideFatherDeceased,

        @NotNull(message = "hasBrideFatherChristianName은 필수입니다.")
        Boolean hasBrideFatherChristianName,

        @Size(min = 2, max = 20, message = "brideFatherName은 2자 이상 20자 이하여야 합니다.")
        String brideFatherName,

        @Size(min = 2, max = 20, message = "brideFatherChristianName은 2자 이상 20자 이하여야 합니다.")
        String brideFatherChristianName,

        /* ---------- 신부 어머님 ---------- */
        @NotNull(message = "brideMotherDeceased는 필수입니다.")
        Boolean brideMotherDeceased,

        @NotNull(message = "hasBrideMotherChristianName은 필수입니다.")
        Boolean hasBrideMotherChristianName,

        @Size(min = 2, max = 20, message = "brideMotherName은 2자 이상 20자 이하여야 합니다.")
        String brideMotherName,

        @Size(min = 2, max = 20, message = "brideMotherChristianName은 2자 이상 20자 이하여야 합니다.")
        String brideMotherChristianName,

        /* ---------- 신부 ---------- */
        @NotNull(message = "hasBrideChristianName은 필수입니다.")
        Boolean hasBrideChristianName,

        @NotBlank(message = "brideName은 필수입니다.")
        @Size(min = 1, max = 20, message = "brideName은 1자 이상 20자 이하여야 합니다.")
        String brideName,

        @Size(min = 2, max = 20, message = "brideChristianName은 2자 이상 20자 이하여야 합니다.")
        String brideChristianName,

        /* ---------- 예식 정보 ---------- */
        @NotNull(message = "weddingDatetime은 필수입니다.")
        @Future(message = "weddingDatetime은 현재보다 이후여야 합니다.")
        @JsonFormat(pattern = "yyyy-MM-dd HH시 mm분", timezone = "Asia/Seoul")
        LocalDateTime weddingDatetime,

        @NotBlank(message = "weddingVenueZoneCode는 공백일 수 없습니다.")
        @Pattern(regexp = "\\d{5}", message = "weddingVenueZoneCode는 5자리 숫자여야 합니다.")
        String weddingVenueZoneCode,

        @NotBlank(message = "weddingVenueAddress는 공백일 수 없습니다.")
        @Size(max = 100, message = "weddingVenueAddress는 최대 100자입니다.")
        String weddingVenueAddress,

        @Size(max = 100, message = "weddingVenueDetailAddress는 최대 100자입니다.")
        String weddingVenueDetailAddress
) {

    @AssertTrue(message = "hasGroomFatherChristianName이 true면 groomFatherChristianName은 필수입니다.")
    private boolean isGroomFatherChristianNameConsistent() {
        return Boolean.FALSE.equals(hasGroomFatherChristianName) || groomFatherChristianName != null;
    }

    @AssertTrue(message = "hasGroomMotherChristianName이 true면 groomMotherChristianName은 필수입니다.")
    private boolean isGroomMotherChristianNameConsistent() {
        return Boolean.FALSE.equals(hasGroomMotherChristianName) || groomMotherChristianName != null;
    }

    @AssertTrue(message = "hasGroomChristianName이 true면 groomChristianName은 필수입니다.")
    private boolean isGroomChristianNameConsistent() {
        return Boolean.FALSE.equals(hasGroomChristianName) || groomChristianName != null;
    }

    @AssertTrue(message = "hasBrideFatherChristianName이 true면 brideFatherChristianName은 필수입니다.")
    private boolean isBrideFatherChristianNameConsistent() {
        return Boolean.FALSE.equals(hasBrideFatherChristianName) || brideFatherChristianName != null;
    }

    @AssertTrue(message = "hasBrideMotherChristianName이 true면 brideMotherChristianName은 필수입니다.")
    private boolean isBrideMotherChristianNameConsistent() {
        return Boolean.FALSE.equals(hasBrideMotherChristianName) || brideMotherChristianName != null;
    }

    @AssertTrue(message = "hasBrideChristianName이 true면 brideChristianName은 필수입니다.")
    private boolean isBrideChristianNameConsistent() {
        return Boolean.FALSE.equals(hasBrideChristianName) || brideChristianName != null;
    }
}
