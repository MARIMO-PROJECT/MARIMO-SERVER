package com.marimo.server.domain.order.service;

import static org.springframework.util.StringUtils.hasText;

import com.marimo.server.domain.order.dto.AdditionalRequestInfo;
import com.marimo.server.domain.order.dto.CharterBus;
import com.marimo.server.domain.order.dto.ContactOption;
import com.marimo.server.domain.order.dto.CustomerInfo;
import com.marimo.server.domain.order.dto.Gallery;
import com.marimo.server.domain.order.dto.GiftAccount;
import com.marimo.server.domain.order.dto.Guestbook;
import com.marimo.server.domain.order.dto.InvitationCommonInfo;
import com.marimo.server.domain.order.dto.InvitationOrderRequest;
import com.marimo.server.domain.order.dto.MobileInvitationInfo;
import com.marimo.server.domain.order.dto.OrderResponse;
import com.marimo.server.domain.order.dto.PaperInvitationInfo;
import com.marimo.server.domain.order.dto.PreVideoCommonInfo;
import com.marimo.server.domain.order.dto.PreVideoOrderRequest;
import com.marimo.server.domain.order.dto.Reception;
import com.marimo.server.domain.order.dto.Rsvp;
import com.marimo.server.domain.order.dto.SelectedOption;
import com.marimo.server.domain.order.entity.InvitationOrderEntity;
import com.marimo.server.domain.order.entity.OrderAttachmentEntity;
import com.marimo.server.domain.order.entity.OrderEntity;
import com.marimo.server.domain.order.enums.AttachmentType;
import com.marimo.server.domain.order.enums.FileType;
import com.marimo.server.domain.order.repository.InvitationOrderRepository;
import com.marimo.server.domain.order.repository.OrderAttachmentRepository;
import com.marimo.server.domain.order.repository.OrderRepository;
import com.marimo.server.domain.product.entity.InvitationOptionEntity;
import com.marimo.server.domain.product.enums.OptionType;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.repository.InvitationOptionRepository;
import com.marimo.server.domain.product.repository.InvitationRepository;
import com.marimo.server.domain.product.repository.PreVideoRepository;
import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_CODE_PREFIX = "MRM";
    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId SEOUL_TIME_ZONE = ZoneId.of("Asia/Seoul");

    private static final EnumSet<FileType> IMAGE_FILE_TYPES = EnumSet.of(FileType.JPG, FileType.JPEG);
    private static final EnumSet<FileType> PRE_VIDEO_FILE_TYPES = EnumSet.of(FileType.JPG, FileType.JPEG, FileType.MP4);
    private static final EnumSet<FileType> INVITATION_REQUEST_FILE_TYPES =
            EnumSet.of(FileType.PNG, FileType.MP4, FileType.MOV, FileType.PDF);
    private static final EnumSet<FileType> PRE_VIDEO_REQUEST_FILE_TYPES =
            EnumSet.of(FileType.PNG, FileType.MOV, FileType.PDF);

    private final InvitationRepository invitationRepository;
    private final InvitationOptionRepository invitationOptionRepository;
    private final InvitationOrderRepository invitationOrderRepository;
    private final OrderAttachmentRepository orderAttachmentRepository;
    private final OrderRepository orderRepository;
    private final PreVideoRepository preVideoRepository;

    @Transactional(readOnly = true)
    public void validateUrlSlug(final String urlSlug) {
        if (invitationOrderRepository.existsByMobileInvitationUrlSlug(urlSlug)) {
            throw new BusinessException(ErrorType.DUPLICATE_URL_SLUG_ERROR);
        }
    }

    @Transactional
    public OrderResponse createInvitationOrder(final InvitationOrderRequest request) {
        long invitationId = request.invitationId();

        if (!invitationRepository.existsById(invitationId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_INVITATION_ERROR);
        }

        boolean hasMobileInvitation = request.hasMobileInvitation();

        if (hasMobileInvitation) {
            validateUrlSlug(request.mobileInvitationInfo().urlSlug());
        }

        validateSelectedOptions(invitationId, request.optionList());

        CustomerInfo customerInfo = request.customerInfo();
        InvitationCommonInfo invitationCommonInfo = request.invitationCommonInfo();
        PaperInvitationInfo paperInvitationInfo = request.paperInvitationInfo();

        boolean hasCharterBus = request.hasCharterBus();
        CharterBus charterBus = hasCharterBus ? request.charterBus() : null;

        boolean hasReception = request.hasReception();
        Reception reception = hasReception ? request.reception() : null;

        MobileInvitationInfo mobileInvitationInfo = hasMobileInvitation ? request.mobileInvitationInfo() : null;

        Boolean hasGallery = request.hasGallery();
        Gallery gallery = Boolean.TRUE.equals(hasGallery) ? request.gallery() : null;

        Boolean hasContactOption = request.hasContactOption();
        ContactOption contactOption = Boolean.TRUE.equals(hasContactOption) ? request.contactOption() : null;

        Boolean hasGiftAccount = request.hasGiftAccount();
        GiftAccount giftAccount = Boolean.TRUE.equals(hasGiftAccount) ? request.giftAccount() : null;

        Boolean hasCalendar = request.hasCalendar();
        Boolean hasMapNavigation = request.hasMapNavigation();

        Boolean hasGuestbook = request.hasGuestbook();
        Guestbook guestbook = Boolean.TRUE.equals(hasGuestbook) ? request.guestbook() : null;

        Boolean hasRsvp = request.hasRsvp();
        Rsvp rsvp = Boolean.TRUE.equals(hasRsvp) ? request.rsvp() : null;

        boolean hasAdditionalRequest = request.hasAdditionalRequest();
        AdditionalRequestInfo additionalRequest = hasAdditionalRequest ? request.additionalRequest() : null;

        OrderEntity orderEntity = OrderEntity.builder()
                .productType(ProductType.INVITATION)
                .productId(invitationId)
                .code(generateUniqueOrderCode())
                .customerName(customerInfo.name())
                .zoneCode(customerInfo.zoneCode())
                .address(customerInfo.address())
                .detailAddress(customerInfo.detailAddress())
                .phoneNumber(customerInfo.phoneNumber())
                .email(customerInfo.email())
                .hasGroomChristianName(invitationCommonInfo.hasGroomChristianName())
                .groomName(invitationCommonInfo.groomName())
                .groomChristianName(invitationCommonInfo.groomChristianName())
                .hasBrideChristianName(invitationCommonInfo.hasBrideChristianName())
                .brideName(invitationCommonInfo.brideName())
                .brideChristianName(invitationCommonInfo.brideChristianName())
                .weddingDateTime(invitationCommonInfo.weddingDateTime())
                .hasAdditionalRequest(hasAdditionalRequest)
                .requestText(additionalRequest != null ? additionalRequest.requestText() : null)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);
        Long orderId = savedOrder.getId();

        InvitationOrderEntity invitationOrderEntity = InvitationOrderEntity.builder()
                .orderId(orderId)

                // 옵션 선택 목록
                .optionList(request.optionList())

                // 양가 혼주 정보
                .groomFatherDeceased(invitationCommonInfo.groomFatherDeceased())
                .hasGroomFatherChristianName(invitationCommonInfo.hasGroomFatherChristianName())
                .groomFatherName(
                        invitationCommonInfo.groomFatherDeceased()
                                ? null
                                : invitationCommonInfo.groomFatherName()
                )
                .groomFatherChristianName(
                        invitationCommonInfo.hasGroomFatherChristianName()
                                ? invitationCommonInfo.groomFatherChristianName()
                                : null
                )

                .groomMotherDeceased(invitationCommonInfo.groomMotherDeceased())
                .hasGroomMotherChristianName(invitationCommonInfo.hasGroomMotherChristianName())
                .groomMotherName(
                        invitationCommonInfo.groomMotherDeceased()
                                ? null
                                : invitationCommonInfo.groomMotherName()
                )
                .groomMotherChristianName(
                        invitationCommonInfo.hasGroomMotherChristianName()
                                ? invitationCommonInfo.groomMotherChristianName()
                                : null
                )

                .brideFatherDeceased(invitationCommonInfo.brideFatherDeceased())
                .hasBrideFatherChristianName(invitationCommonInfo.hasBrideFatherChristianName())
                .brideFatherName(
                        invitationCommonInfo.brideFatherDeceased()
                                ? null
                                : invitationCommonInfo.brideFatherName()
                )
                .brideFatherChristianName(
                        invitationCommonInfo.hasBrideFatherChristianName()
                                ? invitationCommonInfo.brideFatherChristianName()
                                : null
                )

                .brideMotherDeceased(invitationCommonInfo.brideMotherDeceased())
                .hasBrideMotherChristianName(invitationCommonInfo.hasBrideMotherChristianName())
                .brideMotherName(
                        invitationCommonInfo.brideMotherDeceased()
                                ? null
                                : invitationCommonInfo.brideMotherName()
                )
                .brideMotherChristianName(
                        invitationCommonInfo.hasBrideMotherChristianName()
                                ? invitationCommonInfo.brideMotherChristianName()
                                : null
                )

                // 예식장
                .weddingVenueZoneCode(invitationCommonInfo.weddingVenueZoneCode())
                .weddingVenueAddress(invitationCommonInfo.weddingVenueAddress())
                .weddingVenueDetailAddress(invitationCommonInfo.weddingVenueDetailAddress())

                // 종이 청첩장
                .paperInvitationMessage(paperInvitationInfo.message())

                // 전세버스
                .hasCharterBus(hasCharterBus)
                .busStopLocation(charterBus != null ? charterBus.busStopLocation() : null)
                .busStopTimeList(charterBus != null ? charterBus.busStopTimeList() : null)

                // 피로연
                .hasReception(hasReception)
                .receptionAddress(reception != null ? reception.address() : null)
                .receptionDateTime(reception != null ? reception.dateTime() : null)

                // 모바일 청첩장
                .hasMobileInvitation(hasMobileInvitation)
                .mobileInvitationUrlSlug(mobileInvitationInfo != null ? mobileInvitationInfo.urlSlug() : null)
                .mobileInvitationMessage(mobileInvitationInfo != null ? mobileInvitationInfo.message() : null)

                // 갤러리
                .hasGallery(hasGallery)

                // 전화걸기
                .hasContactOption(hasContactOption)
                .groomFatherPhoneNumber(contactOption != null ? contactOption.groomFatherPhoneNumber() : null)
                .groomMotherPhoneNumber(contactOption != null ? contactOption.groomMotherPhoneNumber() : null)
                .groomPhoneNumber(contactOption != null ? contactOption.groomPhoneNumber() : null)
                .brideFatherPhoneNumber(contactOption != null ? contactOption.brideFatherPhoneNumber() : null)
                .brideMotherPhoneNumber(contactOption != null ? contactOption.brideMotherPhoneNumber() : null)
                .bridePhoneNumber(contactOption != null ? contactOption.bridePhoneNumber() : null)

                // 축의금계좌
                .hasGiftAccount(hasGiftAccount)
                .groomGiftAccountList(giftAccount != null ? giftAccount.groomGiftAccountList() : null)
                .brideGiftAccountList(giftAccount != null ? giftAccount.brideGiftAccountList() : null)

                // 달력
                .hasCalendar(hasCalendar)

                // 지도 및 길찾기
                .hasMapNavigation(hasMapNavigation)

                // 방명록
                .hasGuestbook(hasGuestbook)
                .adminPassword(guestbook != null ? guestbook.adminPassword() : null)

                // 참석의사 전달
                .hasRsvp(hasRsvp)
                .hasPrimaryContactField(rsvp != null ? rsvp.hasPrimaryContactField() : null)
                .hasCompanionField(rsvp != null ? rsvp.hasCompanionField() : null)
                .hasMealOptionField(rsvp != null ? rsvp.hasMealOptionField() : null)

                .build();

        invitationOrderRepository.save(invitationOrderEntity);

        List<OrderAttachmentEntity> orderAttachmentEntities = new ArrayList<>();

        // 종이 청첩장 메인 이미지
        String paperInvitationMainImage = paperInvitationInfo.mainImage();

        addAttachmentIfValid(
                orderAttachmentEntities,
                orderId,
                AttachmentType.PAPER_INVITATION_MAIN,
                IMAGE_FILE_TYPES,
                paperInvitationMainImage
        );

        // 모바일 청첩장 메인 이미지
        String mobileInvitationMainImage =
                (hasMobileInvitation && mobileInvitationInfo != null) ? mobileInvitationInfo.mainImage() : null;

        addAttachmentIfValid(
                orderAttachmentEntities,
                orderId,
                AttachmentType.MOBILE_INVITATION_MAIN,
                IMAGE_FILE_TYPES,
                mobileInvitationMainImage
        );

        // 갤러리 이미지
        if (Boolean.TRUE.equals(hasGallery) && gallery != null) {
            for (String imageUrl : gallery.imageList()) {
                addAttachmentIfValid(
                        orderAttachmentEntities,
                        orderId,
                        AttachmentType.GALLERY,
                        IMAGE_FILE_TYPES,
                        imageUrl
                );
            }
        }

        // 기타 요청사항 첨부파일
        if (hasAdditionalRequest && additionalRequest != null) {
            for (String fileUrl : additionalRequest.attachmentList()) {
                addAttachmentIfValid(
                        orderAttachmentEntities,
                        orderId,
                        AttachmentType.INVITATION_REQUEST,
                        INVITATION_REQUEST_FILE_TYPES,
                        fileUrl
                );
            }
        }

        if (!orderAttachmentEntities.isEmpty()) {
            orderAttachmentRepository.saveAll(orderAttachmentEntities);
        }

        return OrderResponse.of(savedOrder.getCode());
    }

    private String generateUniqueOrderCode() {
        String code;

        do {
            code = generateRandomOrderCode();
        } while (orderRepository.existsByCode(code));

        return code;
    }

    private String generateRandomOrderCode() {
        LocalDate now = LocalDate.now(SEOUL_TIME_ZONE);
        String datePart = now.format(ORDER_CODE_DATE_FORMATTER);

        int randomNumber = ThreadLocalRandom.current().nextInt(10_000);
        String suffix = String.format("%04d", randomNumber);

        return ORDER_CODE_PREFIX + datePart + "-" + suffix;
    }

    private void validateSelectedOptions(
            final Long invitationId,
            final List<SelectedOption> selectedOptions
    ) {
        Set<Long> optionIdSet = new HashSet<>();

        for (SelectedOption so : selectedOptions) {
            if (!optionIdSet.add(so.optionId())) {
                throw new BusinessException(ErrorType.DUPLICATE_INVITATION_OPTION_ERROR);
            }
        }

        List<Long> optionIds = new ArrayList<>(optionIdSet);
        List<InvitationOptionEntity> allByIds = invitationOptionRepository.findAllByIdIn(optionIds);

        if (allByIds.size() != optionIds.size()) {
            throw new BusinessException(ErrorType.INVALID_INVITATION_OPTION_ERROR);
        }

        List<InvitationOptionEntity> matchedByInvitation =
                invitationOptionRepository.findAllByInvitationIdAndIdIn(invitationId, optionIds);

        if (matchedByInvitation.size() != optionIds.size()) {
            throw new BusinessException(ErrorType.INVITATION_OPTION_MISMATCH_ERROR);
        }

        long quantityCount = matchedByInvitation.stream()
                .filter(e -> e.getOptionType() == OptionType.QUANTITY)
                .count();

        if (quantityCount == 0) {
            throw new BusinessException(ErrorType.MISSING_REQUIRED_OPTION_ERROR);
        }

        if (quantityCount > 1) {
            throw new BusinessException(ErrorType.MULTIPLE_QUANTITY_OPTION_ERROR);
        }
    }

    private void addAttachmentIfValid(
            final List<OrderAttachmentEntity> orderAttachmentEntities,
            final Long orderId,
            AttachmentType attachmentType,
            final EnumSet<FileType> allowed,
            final String fileUrl
    ) {
        if (!hasText(fileUrl)) {
            return;
        }

        FileType fileType = extractFileTypeFromUrl(fileUrl);
        requireAllowedFileType(fileType, allowed);

        if (fileType == FileType.MP4 && attachmentType == AttachmentType.PRE_VIDEO_IMAGE) {
            attachmentType = AttachmentType.PRE_VIDEO_VIDEO;
        }

        orderAttachmentEntities.add(
                OrderAttachmentEntity.builder()
                        .orderId(orderId)
                        .attachmentType(attachmentType)
                        .fileType(fileType)
                        .fileUrl(fileUrl)
                        .build()
        );
    }

    private FileType extractFileTypeFromUrl(final String fileUrl) {
        if (!fileUrl.contains(".")) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }

        String fileExtension = fileUrl.substring(fileUrl.lastIndexOf('.') + 1);

        return FileType.fromValue(fileExtension);
    }

    private void requireAllowedFileType(FileType fileType, EnumSet<FileType> allowed) {
        if (!allowed.contains(fileType)) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }
    }

    @Transactional
    public OrderResponse createPreVideoOrder(final PreVideoOrderRequest request) {
        long preVideoId = request.preVideoId();

        if (!preVideoRepository.existsById(preVideoId)) {
            throw new BusinessException(ErrorType.NOT_FOUND_PRE_VIDEO_ERROR);
        }

        CustomerInfo customerInfo = request.customerInfo();
        PreVideoCommonInfo preVideoCommonInfo = request.preVideoCommonInfo();

        boolean hasAdditionalRequest = request.hasAdditionalRequest();
        AdditionalRequestInfo additionalRequest = hasAdditionalRequest ? request.additionalRequest() : null;

        OrderEntity orderEntity = OrderEntity.builder()
                .productType(ProductType.PRE_VIDEO)
                .productId(preVideoId)
                .code(generateUniqueOrderCode())
                .customerName(customerInfo.name())
                .zoneCode(customerInfo.zoneCode())
                .address(customerInfo.address())
                .detailAddress(customerInfo.detailAddress())
                .phoneNumber(customerInfo.phoneNumber())
                .email(customerInfo.email())
                .groomName(preVideoCommonInfo.groomName())
                .brideName(preVideoCommonInfo.brideName())
                .weddingDateTime(preVideoCommonInfo.weddingDateTime())
                .hasAdditionalRequest(hasAdditionalRequest)
                .requestText(additionalRequest != null ? additionalRequest.requestText() : null)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);
        Long orderId = savedOrder.getId();

        List<OrderAttachmentEntity> orderAttachmentEntities = new ArrayList<>();

        // 식전영상 사진/영상
        for (String mediaUrl : request.mediaList()) {
            addAttachmentIfValid(
                    orderAttachmentEntities,
                    orderId,
                    AttachmentType.PRE_VIDEO_IMAGE,
                    PRE_VIDEO_FILE_TYPES,
                    mediaUrl
            );
        }

        // 기타 요청사항 첨부파일
        if (hasAdditionalRequest && additionalRequest != null) {
            for (String fileUrl : additionalRequest.attachmentList()) {
                addAttachmentIfValid(
                        orderAttachmentEntities,
                        orderId,
                        AttachmentType.PRE_VIDEO_REQUEST,
                        PRE_VIDEO_REQUEST_FILE_TYPES,
                        fileUrl
                );
            }
        }

        if (!orderAttachmentEntities.isEmpty()) {
            orderAttachmentRepository.saveAll(orderAttachmentEntities);
        }

        return OrderResponse.of(savedOrder.getCode());
    }
}
