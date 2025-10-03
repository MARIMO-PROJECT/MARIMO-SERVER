package com.marimo.server.global.event;

import com.marimo.server.domain.order.dto.request.InvitationOrderRequest;
import com.marimo.server.domain.order.dto.response.OrderResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvitationOrderCreatedEvent {

    private final InvitationOrderRequest request;
    private final OrderResponse response;
}
