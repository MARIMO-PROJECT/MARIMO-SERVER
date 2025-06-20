package com.marimo.server.domain.product.service;

import com.marimo.server.domain.product.dto.BannerListResponse;
import com.marimo.server.domain.product.dto.BannerResponse;
import com.marimo.server.domain.product.entity.BannerEntity;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.repository.BannerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public BannerListResponse fetchBanners(final ProductType productType) {
        List<BannerEntity> bannerEntities =
                bannerRepository.findAllByProductTypeOrderById(productType);

        List<BannerResponse> bannerResponses = bannerEntities.stream()
                .map(banner -> BannerResponse.of(
                        banner.getFileUrl(),
                        banner.getCtaUrl()
                ))
                .toList();

        return BannerListResponse.of(bannerResponses);
    }
}
