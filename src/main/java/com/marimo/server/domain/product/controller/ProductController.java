package com.marimo.server.domain.product.controller;

import com.marimo.server.domain.product.dto.BannerListResponse;
import com.marimo.server.domain.product.dto.InvitationListResponse;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.service.ProductService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
}
