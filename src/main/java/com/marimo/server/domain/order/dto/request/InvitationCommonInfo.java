package com.marimo.server.domain.order.dto.request;

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
        @NotNull(message = "weddingDateTime은 필수입니다.")
        @Future(message = "weddingDateTime은 현재보다 이후여야 합니다.")
        @JsonFormat(pattern = "yyyy-MM-dd HH시 mm분", timezone = "Asia/Seoul")
        LocalDateTime weddingDateTime,

        @NotBlank(message = "weddingVenueZoneCode는 공백일 수 없습니다.")
        @Pattern(regexp = "\\d{5}", message = "weddingVenueZoneCode는 5자리 숫자여야 합니다.")
        String weddingVenueZoneCode,

        @NotBlank(message = "weddingVenueAddress는 공백일 수 없습니다.")
        @Size(max = 100, message = "weddingVenueAddress는 최대 100자입니다.")
        String weddingVenueAddress,

        @Size(max = 100, message = "weddingVenueDetailAddress는 최대 100자입니다.")
        String weddingVenueDetailAddress
) {

    private static boolean flagMatchesObj(Boolean flag, Object obj) {
        if (flag == null) {
            return false;
        }

        return flag == (obj != null);
    }

    @AssertTrue(message = "groomFatherDeceased와 groomFatherName의 상태가 일치하지 않습니다. (true → null, false → 필수)")
    private boolean isGroomFatherNameStrict() {
        return flagMatchesObj(!groomFatherDeceased, groomFatherName);
    }

    @AssertTrue(message = "hasGroomFatherChristianName와 groomFatherChristianName의 상태가 일치하지 않습니다. (true → 필수, false → null)")
    private boolean isGroomFatherChristianNameStrict() {
        return flagMatchesObj(hasGroomFatherChristianName, groomFatherChristianName);
    }

    @AssertTrue(message = "groomMotherDeceased와 groomMotherName의 상태가 일치하지 않습니다. (true → null, false → 필수)")
    private boolean isGroomMotherNameStrict() {
        return flagMatchesObj(!groomMotherDeceased, groomMotherName);
    }

    @AssertTrue(message = "hasGroomMotherChristianName와 groomMotherChristianName의 상태가 일치하지 않습니다. (true → 필수, false → null)")
    private boolean isGroomMotherChristianNameStrict() {
        return flagMatchesObj(hasGroomMotherChristianName, groomMotherChristianName);
    }

    @AssertTrue(message = "hasGroomChristianName와 groomChristianName의 상태가 일치하지 않습니다. (true → 필수, false → null)")
    private boolean isGroomChristianNameStrict() {
        return flagMatchesObj(hasGroomChristianName, groomChristianName);
    }

    @AssertTrue(message = "brideFatherDeceased와 brideFatherName의 상태가 일치하지 않습니다. (true → null, false → 필수)")
    private boolean isBrideFatherNameStrict() {
        return flagMatchesObj(!brideFatherDeceased, brideFatherName);
    }

    @AssertTrue(message = "hasBrideFatherChristianName와 brideFatherChristianName의 상태가 일치하지 않습니다. (true → 필수, false → null)")
    private boolean isBrideFatherChristianNameStrict() {
        return flagMatchesObj(hasBrideFatherChristianName, brideFatherChristianName);
    }

    @AssertTrue(message = "brideMotherDeceased와 brideMotherName의 상태가 일치하지 않습니다. (true → null, false → 필수)")
    private boolean isBrideMotherNameStrict() {
        return flagMatchesObj(!brideMotherDeceased, brideMotherName);
    }

    @AssertTrue(message = "hasBrideMotherChristianName와 brideMotherChristianName의 상태가 일치하지 않습니다. (true → 필수, false → null)")
    private boolean isBrideMotherChristianNameStrict() {
        return flagMatchesObj(hasBrideMotherChristianName, brideMotherChristianName);
    }

    @AssertTrue(message = "hasBrideChristianName와 brideChristianName의 상태가 일치하지 않습니다. (true → 필수, false → null)")
    private boolean isBrideChristianNameStrict() {
        return flagMatchesObj(hasBrideChristianName, brideChristianName);
    }
}
