package com.marimo.server.domain.order.controller;

import com.marimo.server.domain.order.dto.InvitationOrderRequest;
import com.marimo.server.domain.order.dto.OrderResponse;
import com.marimo.server.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping(
            path = "/invitations",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrderResponse> createInvitationOrder(
            @Valid @RequestBody final InvitationOrderRequest request
    ) {
        return ResponseEntity.ok(
                orderService.createInvitationOrder(request)
        );
    }
}
