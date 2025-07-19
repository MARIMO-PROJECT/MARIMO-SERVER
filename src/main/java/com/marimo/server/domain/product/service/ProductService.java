package com.marimo.server.domain.product.service;

import com.marimo.server.domain.product.dto.BannerListResponse;
import com.marimo.server.domain.product.dto.BannerResponse;
import com.marimo.server.domain.product.dto.InvitationDetailResponse;
import com.marimo.server.domain.product.dto.InvitationListResponse;
import com.marimo.server.domain.product.dto.InvitationResponse;
import com.marimo.server.domain.product.dto.OptionGroupResponse;
import com.marimo.server.domain.product.dto.OptionResponse;
import com.marimo.server.domain.product.dto.PreVideoDetailResponse;
import com.marimo.server.domain.product.dto.PreVideoListResponse;
import com.marimo.server.domain.product.dto.PreVideoResponse;
import com.marimo.server.domain.product.entity.BannerEntity;
import com.marimo.server.domain.product.entity.InvitationEntity;
import com.marimo.server.domain.product.entity.InvitationOptionEntity;
import com.marimo.server.domain.product.entity.PreVideoEntity;
import com.marimo.server.domain.product.entity.ProductImageEntity;
import com.marimo.server.domain.product.enums.ImageType;
import com.marimo.server.domain.product.enums.OptionType;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.repository.BannerRepository;
import com.marimo.server.domain.product.repository.InvitationOptionRepository;
import com.marimo.server.domain.product.repository.InvitationRepository;
import com.marimo.server.domain.product.repository.PreVideoRepository;
import com.marimo.server.domain.product.repository.ProductImageRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final BannerRepository bannerRepository;
    private final InvitationOptionRepository invitationOptionRepository;
    private final InvitationRepository invitationRepository;
    private final PreVideoRepository preVideoRepository;
    private final ProductImageRepository productImageRepository;

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
        Map<Long, String> invitationImageMap = findImageMapByImageType(ImageType.INVITATION);

        Map<Long, InvitationOptionEntity> invitationOptionMap =
                invitationOptionRepository.findAllByOptionTypeOrderById(OptionType.QUANTITY).stream()
                        .collect(Collectors.toMap(
                                InvitationOptionEntity::getInvitationId,
                                Function.identity(),
                                (existing, replacement) -> existing.getPrice() <= replacement.getPrice()
                                        ? existing
                                        : replacement
                        ));

        List<InvitationResponse> invitationResponses = invitationRepository.findAllByOrderById().stream()
                .filter(invitation ->
                        invitationImageMap.containsKey(invitation.getId())
                                && invitationOptionMap.containsKey(invitation.getId()))
                .map(invitation -> {
                    String imageUrl = invitationImageMap.get(invitation.getId());
                    InvitationOptionEntity option = invitationOptionMap.get(invitation.getId());

                    return InvitationResponse.of(
                            invitation.getId(),
                            imageUrl,
                            invitation.getHasBundle(),
                            invitation.getName(),
                            invitation.getDiscountRate(),
                            option.getPrice(),
                            option.getName()
                    );
                })
                .toList();

        return InvitationListResponse.of(invitationResponses);
    }

    private Map<Long, String> findImageMapByImageType(final ImageType imageType) {
        return productImageRepository.findAllByImageTypeOrderById(imageType).stream()
                .collect(Collectors.toMap(
                        ProductImageEntity::getProductId,
                        ProductImageEntity::getImageUrl,
                        (existing, replacement) -> existing
                ));
    }

    @Transactional(readOnly = true)
    public InvitationDetailResponse fetchInvitationDetail(final Long invitationId) {
        InvitationEntity invitationEntity = invitationRepository.findByIdOrElseThrow(invitationId);

        List<ProductImageEntity> productImageEntities =
                productImageRepository.findAllByProductIdOrderById(invitationId);

        String mainImageUrl = productImageEntities.stream()
                .filter(productImage -> productImage.getImageType() == ImageType.INVITATION)
                .findFirst()
                .map(ProductImageEntity::getImageUrl)
                .orElse("https://github.com/user-attachments/assets/39ea69aa-1a75-4286-b5a9-b6da5e1eebce"); // TODO: 기본 이미지 생기면 변경

        List<String> detailImages = productImageEntities.stream()
                .filter(productImage -> productImage.getImageType() == ImageType.INVITATION_DETAIL)
                .map(ProductImageEntity::getImageUrl)
                .toList();

        Map<OptionType, List<OptionResponse>> optionListMap =
                invitationOptionRepository.findAllByInvitationIdOrderById(invitationId).stream()
                        .collect(Collectors.groupingBy(
                                InvitationOptionEntity::getOptionType,
                                Collectors.mapping(option -> OptionResponse.of(
                                        option.getId(),
                                        option.getName(),
                                        option.getOptionDetail(),
                                        option.getPrice()
                                ), Collectors.toUnmodifiableList())
                        ));

        int price = optionListMap.getOrDefault(OptionType.QUANTITY, List.of()).stream()
                .mapToInt(OptionResponse::price)
                .min()
                .orElse(0);

        List<OptionGroupResponse> optionGroupResponses = Arrays.stream(OptionType.values())
                .map(optionType -> OptionGroupResponse.of(
                        optionType,
                        optionListMap.getOrDefault(optionType, List.of())
                ))
                .filter(optionGroup -> !optionGroup.optionList().isEmpty())
                .toList();

        return InvitationDetailResponse.of(
                mainImageUrl,
                invitationEntity.getName(),
                invitationEntity.getDiscountRate(),
                price,
                invitationEntity.getDescription(),
                optionGroupResponses,
                detailImages
        );
    }

    @Transactional(readOnly = true)
    public PreVideoListResponse fetchPreVideos() {
        Map<Long, String> preVideoImageMap = findImageMapByImageType(ImageType.PRE_VIDEO_THUMBNAIL);

        List<PreVideoResponse> preVideoResponses = preVideoRepository.findAllByOrderById().stream()
                .filter(preVideo -> preVideoImageMap.containsKey(preVideo.getId()))
                .map(preVideo -> {
                    String imageUrl = preVideoImageMap.get(preVideo.getId());

                    return PreVideoResponse.of(
                            preVideo.getId(),
                            imageUrl,
                            preVideo.getSampleVideoUrl(),
                            preVideo.getName(),
                            preVideo.getDiscountRate(),
                            preVideo.getPrice()
                    );
                })
                .toList();

        return PreVideoListResponse.of(preVideoResponses);
    }

    @Transactional(readOnly = true)
    public PreVideoDetailResponse fetchPreVideoDetail(final Long preVideoId) {
        String mainImageUrl =
                productImageRepository.findFirstImageUrlByImageTypeAndProductId(
                                ImageType.PRE_VIDEO_MAIN.name(),
                                preVideoId
                        )
                        .orElse("https://github.com/user-attachments/assets/e62c107d-b60a-432a-a7f1-bcd8ccf815ee"); // TODO: 기본 이미지 생기면 변경

        PreVideoEntity preVideoEntity = preVideoRepository.findByIdOrElseThrow(preVideoId);

        return PreVideoDetailResponse.of(
                mainImageUrl,
                preVideoEntity.getName(),
                preVideoEntity.getDiscountRate(),
                preVideoEntity.getPrice(),
                preVideoEntity.getDescription(),
                preVideoEntity.getSampleVideoUrl()
        );
    }
}
