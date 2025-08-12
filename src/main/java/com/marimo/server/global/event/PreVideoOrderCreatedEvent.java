package com.marimo.server.global.event;

import com.marimo.server.domain.order.dto.request.PreVideoOrderRequest;
import com.marimo.server.domain.order.dto.response.OrderResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PreVideoOrderCreatedEvent {

    private final PreVideoOrderRequest request;
    private final OrderResponse response;
}
