package com.marimo.server.domain.product.controller;

import com.marimo.server.domain.product.dto.BannerListResponse;
import com.marimo.server.domain.product.dto.InvitationDetailResponse;
import com.marimo.server.domain.product.dto.InvitationListResponse;
import com.marimo.server.domain.product.dto.PreVideoDetailResponse;
import com.marimo.server.domain.product.dto.PreVideoListResponse;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.service.ProductService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping(path = "/banners", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BannerListResponse> getBanners(
            @NotBlank(message = "productType은 공백일 수 없습니다.")
            @RequestParam(name = "productType") final String productTypeString
    ) {
        ProductType productType = ProductType.fromValue(productTypeString);

        return ResponseEntity.ok(
                productService.fetchBanners(productType)
        );
    }

    @GetMapping(path = "/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvitationListResponse> getInvitations() {
        return ResponseEntity.ok(
                productService.fetchInvitations()
        );
    }

    @GetMapping(path = "/invitations/{invitationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvitationDetailResponse> getInvitationDetail(
            @NotNull(message = "invitationId는 필수입니다.")
            @Positive(message = "invitationId는 양수여야 합니다.")
            @PathVariable(name = "invitationId") final Long invitationId
    ) {
        return ResponseEntity.ok(
                productService.fetchInvitationDetail(invitationId)
        );
    }

    @GetMapping(path = "/pre-videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PreVideoListResponse> getPreVideos() {
        return ResponseEntity.ok(
                productService.fetchPreVideos()
        );
    }

    @GetMapping(path = "/pre-videos/{preVideoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PreVideoDetailResponse> getPreVideoDetail(
            @NotNull(message = "preVideoId는 필수입니다.")
            @Positive(message = "preVideoId는 양수여야 합니다.")
            @PathVariable(name = "preVideoId") final Long preVideoId
    ) {
        return ResponseEntity.ok(
                productService.fetchPreVideoDetail(preVideoId)
        );
    }
}
