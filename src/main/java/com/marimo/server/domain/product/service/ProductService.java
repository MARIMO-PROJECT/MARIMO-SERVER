package com.marimo.server.domain.product.service;

import com.marimo.server.domain.product.dto.BannerListResponse;
import com.marimo.server.domain.product.dto.BannerResponse;
import com.marimo.server.domain.product.dto.InvitationListResponse;
import com.marimo.server.domain.product.dto.InvitationResponse;
import com.marimo.server.domain.product.entity.BannerEntity;
import com.marimo.server.domain.product.entity.InvitationEntity;
import com.marimo.server.domain.product.entity.InvitationOptionEntity;
import com.marimo.server.domain.product.entity.ProductImageEntity;
import com.marimo.server.domain.product.enums.ImageType;
import com.marimo.server.domain.product.enums.OptionType;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.repository.BannerRepository;
import com.marimo.server.domain.product.repository.InvitationOptionRepository;
import com.marimo.server.domain.product.repository.InvitationRepository;
import com.marimo.server.domain.product.repository.ProductImageRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final BannerRepository bannerRepository;
    private final InvitationRepository invitationRepository;
    private final ProductImageRepository productImageRepository;
    private final InvitationOptionRepository invitationOptionRepository;

    @Transactional(readOnly = true)
    public BannerListResponse fetchBanners(final ProductType productType) {
        List<BannerEntity> bannerEntities = bannerRepository.findAllByProductTypeAndActiveTrueOrderByIdDesc(
                productType);

        List<BannerResponse> bannerResponses = bannerEntities.stream()
                .map(banner -> BannerResponse.of(
                        banner.getFileUrl(),
                        banner.getCtaUrl()
                ))
                .toList();

        return BannerListResponse.of(bannerResponses);
    }

    @Transactional(readOnly = true)
    public InvitationListResponse fetchInvitations() {
        List<InvitationEntity> invitationEntities = invitationRepository.findAllByOrderById();
        List<ProductImageEntity> productImageEntities =
                productImageRepository.findAllByImageTypeOrderByProductIdAscIdAsc(ImageType.INVITATION);
        List<InvitationOptionEntity> invitationOptionEntities =
                invitationOptionRepository.findAllByOptionTypeOrderById(OptionType.QUANTITY);
        List<InvitationResponse> invitationResponses = new ArrayList<>();

        for (InvitationEntity invitation : invitationEntities) {
            ProductImageEntity productImage = productImageEntities.stream()
                    .filter(image -> image.getProductId().equals(invitation.getId()))
                    .findFirst()
                    .orElse(null);

            InvitationOptionEntity invitationOption = invitationOptionEntities.stream()
                    .filter(option -> option.getInvitationId().equals(invitation.getId()))
                    .findFirst()
                    .orElse(null);

            if (productImage == null || invitationOption == null) {
                continue;
            }

            invitationResponses.add(
                    InvitationResponse.of(
                            invitation.getId(),
                            productImage.getImageUrl(),
                            invitation.getHasBundle(),
                            invitation.getName(),
                            invitation.getDiscountRate(),
                            invitationOption.getPrice(),
                            invitationOption.getName()
                    )
            );
        }

        return InvitationListResponse.of(invitationResponses);
    }
}
