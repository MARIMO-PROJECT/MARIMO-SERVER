package com.marimo.server.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record InvitationOrderRequest(
        @NotNull(message = "invitationId는 필수입니다.")
        @Positive(message = "invitationId는 양수여야 합니다.")
        Long invitationId,

        @NotEmpty(message = "optionList에는 최소 1개 이상의 항목이 필요합니다.")
        List<@NotNull(message = "SelectedOption은 null이 될 수 없습니다.") @Valid SelectedOption> optionList,

        @NotNull(message = "customerInfo는 필수입니다.")
        @Valid CustomerInfo customerInfo,

        @NotNull(message = "invitationCommonInfo는 필수입니다.")
        @Valid InvitationCommonInfo invitationCommonInfo,

        @NotNull(message = "paperInvitationInfo는 필수입니다.")
        @Valid PaperInvitationInfo paperInvitationInfo,

        @NotNull(message = "hasCharterBus는 필수입니다.")
        Boolean hasCharterBus,

        @Valid CharterBus charterBus,

        @NotNull(message = "hasReception은 필수입니다.")
        Boolean hasReception,

        @Valid Reception reception,

        @NotNull(message = "hasMobileInvitation은 필수입니다.")
        Boolean hasMobileInvitation,

        @Valid MobileInvitationInfo mobileInvitationInfo,

        Boolean hasGallery,

        @Valid Gallery gallery,

        Boolean hasContactOption,

        @Valid ContactOption contactOption,

        Boolean hasGiftAccount,

        @Valid GiftAccount giftAccount,

        Boolean hasCalendar,

        Boolean hasMapNavigation,

        Boolean hasGuestbook,

        @Valid Guestbook guestbook,

        Boolean hasRsvp,

        @Valid Rsvp rsvp,

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

    @AssertTrue(message = "hasCharterBus와 charterBus의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isCharterBusStrict() {
        return flagMatchesObj(hasCharterBus, charterBus);
    }

    @AssertTrue(message = "hasReception과 reception의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isReceptionStrict() {
        return flagMatchesObj(hasReception, reception);
    }

    @AssertTrue(message = "hasMobileInvitation과 모바일 청첩장 관련 필드들의 상태가 일치하지 않습니다. (true → 모두 필수, false → 모두 null)")
    private boolean isMobileInvitationStrict() {
        if (Boolean.TRUE.equals(hasMobileInvitation)) {
            return mobileInvitationInfo != null
                    && hasGallery != null
                    && hasContactOption != null
                    && hasGiftAccount != null
                    && hasCalendar != null
                    && hasMapNavigation != null
                    && hasGuestbook != null
                    && hasRsvp != null;
        }

        return mobileInvitationInfo == null
                && hasGallery == null
                && gallery == null
                && hasContactOption == null
                && contactOption == null
                && hasGiftAccount == null
                && giftAccount == null
                && hasCalendar == null
                && hasMapNavigation == null
                && hasGuestbook == null
                && guestbook == null
                && hasRsvp == null
                && rsvp == null;
    }

    @AssertTrue(message = "hasGallery와 gallery의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isGalleryStrict() {
        return flagMatchesObj(hasGallery, gallery);
    }

    @AssertTrue(message = "hasContactOption과 contactOption의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isContactOptionStrict() {
        return flagMatchesObj(hasContactOption, contactOption);
    }

    @AssertTrue(message = "hasGiftAccount와 giftAccount의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isGiftAccountStrict() {
        return flagMatchesObj(hasGiftAccount, giftAccount);
    }

    @AssertTrue(message = "hasGuestbook과 guestbook의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isGuestbookStrict() {
        return flagMatchesObj(hasGuestbook, guestbook);
    }

    @AssertTrue(message = "hasRsvp와 rsvp의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isRsvpStrict() {
        return flagMatchesObj(hasRsvp, rsvp);
    }

    @AssertTrue(message = "hasAdditionalRequest와 additionalRequest의 상태가 일치하지 않습니다. (true → 객체 필수, false → null)")
    private boolean isAdditionalRequestStrict() {
        return flagMatchesObj(hasAdditionalRequest, additionalRequest);
    }
}
