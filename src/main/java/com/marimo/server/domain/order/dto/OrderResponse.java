package com.marimo.server.domain.order.dto;

public record OrderResponse(
        String orderCode
) {

    public static OrderResponse of(final String orderCode) {
        return new OrderResponse(orderCode);
    }
}
