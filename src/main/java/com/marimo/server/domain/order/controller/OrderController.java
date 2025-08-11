package com.marimo.server.domain.order.controller;

import com.marimo.server.domain.order.dto.request.InvitationOrderRequest;
import com.marimo.server.domain.order.dto.request.PreVideoOrderRequest;
import com.marimo.server.domain.order.dto.request.PresignedUrlRequest;
import com.marimo.server.domain.order.dto.response.OrderResponse;
import com.marimo.server.domain.order.dto.response.PresignedUrlListResponse;
import com.marimo.server.domain.order.enums.AttachmentType;
import com.marimo.server.domain.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping(path = "/mobile-invitations/validate")
    public ResponseEntity<Void> getUrlSlugValidation(
            @NotBlank(message = "urlSlug는 공백일 수 없습니다.")
            @Pattern(regexp = "^[a-z0-9]{3,15}$", message = "urlSlug는 영문 소문자와 숫자만 사용할 수 있으며 3~15자 사이여야 합니다.")
            @RequestParam final String urlSlug
    ) {
        orderService.validateUrlSlug(urlSlug);

        return ResponseEntity.ok().build();
    }

    @PostMapping(
            path = "/orders/invitations",
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

    @PostMapping(
            path = "/orders/pre-videos",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrderResponse> createPreVideoOrder(
            @Valid @RequestBody final PreVideoOrderRequest request
    ) {
        return ResponseEntity.ok(
                orderService.createPreVideoOrder(request)
        );
    }

    @PostMapping(
            path = "/files/presigned-url",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PresignedUrlListResponse> issuePresignedUrls(
            @Valid @RequestBody final PresignedUrlRequest request
    ) {
        AttachmentType attachmentType = AttachmentType.fromValue(request.attachmentType());

        return ResponseEntity.ok(
                orderService.issuePresignedUrls(attachmentType, request.uploadFileInfoList())
        );
    }
}
