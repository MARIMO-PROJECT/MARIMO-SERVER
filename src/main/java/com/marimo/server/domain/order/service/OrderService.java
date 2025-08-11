package com.marimo.server.domain.order.service;

import static org.springframework.util.StringUtils.hasText;

import com.marimo.server.domain.order.dto.request.AdditionalRequestInfo;
import com.marimo.server.domain.order.dto.request.CharterBus;
import com.marimo.server.domain.order.dto.request.ContactOption;
import com.marimo.server.domain.order.dto.request.CustomerInfo;
import com.marimo.server.domain.order.dto.request.Gallery;
import com.marimo.server.domain.order.dto.request.GiftAccount;
import com.marimo.server.domain.order.dto.request.Guestbook;
import com.marimo.server.domain.order.dto.request.InvitationCommonInfo;
import com.marimo.server.domain.order.dto.request.InvitationOrderRequest;
import com.marimo.server.domain.order.dto.request.MobileInvitationInfo;
import com.marimo.server.domain.order.dto.request.PaperInvitationInfo;
import com.marimo.server.domain.order.dto.request.PreVideoCommonInfo;
import com.marimo.server.domain.order.dto.request.PreVideoOrderRequest;
import com.marimo.server.domain.order.dto.request.Reception;
import com.marimo.server.domain.order.dto.request.Rsvp;
import com.marimo.server.domain.order.dto.request.SelectedOption;
import com.marimo.server.domain.order.dto.request.UploadFileInfo;
import com.marimo.server.domain.order.dto.response.OrderResponse;
import com.marimo.server.domain.order.dto.response.PresignedUrl;
import com.marimo.server.domain.order.dto.response.PresignedUrlListResponse;
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
import com.marimo.server.global.adapter.S3Adapter;
import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_CODE_PREFIX = "MRM";
    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId SEOUL_TIME_ZONE = ZoneId.of("Asia/Seoul");
    private static final int ORDER_CODE_RANDOM_BOUND = 10_000; // 0000~9999
    private static final String ORDER_CODE_SUFFIX_FORMAT = "%04d";

    private static final String S3_KEY_PREFIX_ORDERS_TEMP = "orders/temp";
    private static final String DEFAULT_BASE_NAME = "file";
    private static final int BASE_NAME_MAX_LENGTH = 100;

    private static final Pattern REPEATED_UNDERSCORES = Pattern.compile("_+");
    private static final Pattern TRIM_EDGE_UNDERSCORES = Pattern.compile("^_+|_+$");
    private static final Pattern DISALLOWED_BASE_NAME_CHARS_PATTERN = Pattern.compile("[^0-9A-Za-z가-힣ㄱ-ㅎㅏ-ㅣ._-]");

    private final S3Adapter s3Adapter;

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
        String paperInvitationMainImageUrl = paperInvitationInfo.mainImage();

        validateAndAddAttachment(
                orderAttachmentEntities,
                orderId,
                AttachmentType.PAPER_INVITATION_MAIN,
                paperInvitationMainImageUrl
        );

        // 모바일 청첩장 메인 이미지
        String mobileInvitationMainImageUrl =
                (hasMobileInvitation && mobileInvitationInfo != null) ? mobileInvitationInfo.mainImage() : null;

        validateAndAddAttachment(
                orderAttachmentEntities,
                orderId,
                AttachmentType.MOBILE_INVITATION_MAIN,
                mobileInvitationMainImageUrl
        );

        // 갤러리 이미지
        if (Boolean.TRUE.equals(hasGallery) && gallery != null) {
            for (String galleryImageUrl : gallery.imageList()) {
                validateAndAddAttachment(
                        orderAttachmentEntities,
                        orderId,
                        AttachmentType.GALLERY,
                        galleryImageUrl
                );
            }
        }

        // 기타 요청사항 첨부파일
        if (hasAdditionalRequest && additionalRequest != null) {
            for (String fileUrl : additionalRequest.attachmentList()) {
                validateAndAddAttachment(
                        orderAttachmentEntities,
                        orderId,
                        AttachmentType.INVITATION_REQUEST,
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

        int randomNumber = ThreadLocalRandom.current().nextInt(ORDER_CODE_RANDOM_BOUND);
        String suffix = String.format(ORDER_CODE_SUFFIX_FORMAT, randomNumber);

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

    private void validateAndAddAttachment(
            final List<OrderAttachmentEntity> target,
            final Long orderId,
            final AttachmentType attachmentType,
            final String fileUrl
    ) {
        if (!hasText(fileUrl)) {
            return;
        }

        FileType fileType = resolveFileTypeFromPath(fileUrl);

        if (!attachmentType.isAllowed(fileType)) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }

        target.add(
                OrderAttachmentEntity.builder()
                        .orderId(orderId)
                        .attachmentType(attachmentType)
                        .fileType(fileType)
                        .fileUrl(fileUrl)
                        .build()
        );
    }

    private FileType resolveFileTypeFromPath(final String path) {
        if (!StringUtils.hasText(path) || !path.contains(".")) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }

        String ext = path.substring(path.lastIndexOf(".") + 1);

        return FileType.fromValue(ext);
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
            validateAndAddAttachment(
                    orderAttachmentEntities,
                    orderId,
                    AttachmentType.PRE_VIDEO,
                    mediaUrl
            );
        }

        // 기타 요청사항 첨부파일
        if (hasAdditionalRequest && additionalRequest != null) {
            for (String fileUrl : additionalRequest.attachmentList()) {
                validateAndAddAttachment(
                        orderAttachmentEntities,
                        orderId,
                        AttachmentType.PRE_VIDEO_REQUEST,
                        fileUrl
                );
            }
        }

        if (!orderAttachmentEntities.isEmpty()) {
            orderAttachmentRepository.saveAll(orderAttachmentEntities);
        }

        return OrderResponse.of(savedOrder.getCode());
    }

    public PresignedUrlListResponse issuePresignedUrls(
            final AttachmentType attachmentType,
            final List<UploadFileInfo> uploadFileInfoList
    ) {
        if (uploadFileInfoList.size() > attachmentType.getMaxCount()) {
            throw new BusinessException(ErrorType.FILE_COUNT_LIMIT_EXCEEDED_ERROR);
        }

        List<PresignedUrl> results = new ArrayList<>(uploadFileInfoList.size());

        for (UploadFileInfo uploadFileInfo : uploadFileInfoList) {
            FileType fileType = resolveFileTypeFromPath(uploadFileInfo.originalFileName());

            if (!attachmentType.isAllowed(fileType)) {
                throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
            }

            if (uploadFileInfo.fileSizeBytes() > fileType.maxSizeBytes()) {
                throw new BusinessException(ErrorType.FILE_SIZE_LIMIT_EXCEEDED_ERROR);
            }

            String extLower = fileType.name().toLowerCase(Locale.ROOT);
            String baseName = sanitizeBaseName(extractBaseName(uploadFileInfo.originalFileName()));
            String s3Key = String.join("/",
                    S3_KEY_PREFIX_ORDERS_TEMP,
                    attachmentType.name().toLowerCase(Locale.ROOT),
                    baseName + "_" + UUID.randomUUID() + "." + extLower
            );

            String presignedUrl;

            try {
                presignedUrl = s3Adapter.issuePresignedUrl(s3Key, fileType);
            } catch (Exception e) {
                throw new BusinessException(ErrorType.FAILED_GET_PRESIGNED_URL_ERROR);
            }

            results.add(PresignedUrl.of(s3Key, presignedUrl));
        }

        return PresignedUrlListResponse.of(results);
    }

    private String sanitizeBaseName(final String rawBaseName) {
        if (!StringUtils.hasText(rawBaseName)) {
            return DEFAULT_BASE_NAME;
        }

        // 유니코드 정규화 (한글 조합형 → NFC)
        String nfc = Normalizer.normalize(rawBaseName, Normalizer.Form.NFC);

        // 비허용 문자 '_'로 치환
        String sanitized = DISALLOWED_BASE_NAME_CHARS_PATTERN.matcher(nfc).replaceAll("_");

        // 연속된 '_' 축약
        sanitized = REPEATED_UNDERSCORES.matcher(sanitized).replaceAll("_");

        // 앞뒤 '_' 제거
        sanitized = TRIM_EDGE_UNDERSCORES.matcher(sanitized).replaceAll("");

        // 비어버린 경우 대비
        if (sanitized.isBlank()) {
            sanitized = DEFAULT_BASE_NAME;
        }

        // 길이 제한
        if (sanitized.length() > BASE_NAME_MAX_LENGTH) {
            sanitized = sanitized.substring(0, BASE_NAME_MAX_LENGTH);
        }

        return sanitized;
    }

    private String extractBaseName(final String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }

        int idx = fileName.lastIndexOf(".");

        // idx <= 0 : 확장자 없음(-1) 또는 선행 점파일(.env)
        // idx == len-1 : 'a.'처럼 확장자 비어 있음
        if (idx <= 0 || idx == fileName.length() - 1) {
            throw new BusinessException(ErrorType.INVALID_FILE_TYPE_ERROR);
        }

        return fileName.substring(0, idx);
    }
}
