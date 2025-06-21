package com.marimo.server.domain.product.dto;

import java.util.List;

public record InvitationListResponse(
        List<InvitationResponse> invitationList
) {

    public static InvitationListResponse of(final List<InvitationResponse> invitationList) {
        return new InvitationListResponse(invitationList);
    }
}
