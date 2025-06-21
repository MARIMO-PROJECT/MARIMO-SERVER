package com.marimo.server.domain.product.service;

import com.marimo.server.domain.product.dto.BannerListResponse;
import com.marimo.server.domain.product.dto.BannerResponse;
import com.marimo.server.domain.product.dto.InvitationListResponse;
import com.marimo.server.domain.product.dto.InvitationResponse;
import com.marimo.server.domain.product.entity.BannerEntity;
import com.marimo.server.domain.product.entity.InvitationOptionEntity;
import com.marimo.server.domain.product.entity.ProductImageEntity;
import com.marimo.server.domain.product.enums.ImageType;
import com.marimo.server.domain.product.enums.OptionType;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.repository.BannerRepository;
import com.marimo.server.domain.product.repository.InvitationOptionRepository;
import com.marimo.server.domain.product.repository.InvitationRepository;
import com.marimo.server.domain.product.repository.ProductImageRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        List<BannerEntity> bannerEntities =
                bannerRepository.findAllByProductTypeAndActiveTrueOrderByIdDesc(productType);

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
        Map<Long, String> invitationImageMap =
                productImageRepository.findAllByImageTypeOrderById(ImageType.INVITATION).stream()
                        .collect(Collectors.toMap(
                                ProductImageEntity::getProductId,
                                ProductImageEntity::getImageUrl,
                                (existing, replacement) -> existing
                        ));

        Map<Long, InvitationOptionEntity> invitationOptionMap =
                invitationOptionRepository.findAllByOptionTypeOrderById(OptionType.QUANTITY).stream()
                        .collect(Collectors.toMap(
                                InvitationOptionEntity::getInvitationId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        List<InvitationResponse> invitationResponses = invitationRepository.findAllByOrderById().stream()
                .map(invitation -> {
                    String image = invitationImageMap.get(invitation.getId());
                    InvitationOptionEntity option = invitationOptionMap.get(invitation.getId());

                    if (image == null || option == null) {
                        return null;
                    }

                    return InvitationResponse.of(
                            invitation.getId(),
                            image,
                            invitation.getHasBundle(),
                            invitation.getName(),
                            invitation.getDiscountRate(),
                            option.getPrice(),
                            option.getName()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return InvitationListResponse.of(invitationResponses);
    }
}
