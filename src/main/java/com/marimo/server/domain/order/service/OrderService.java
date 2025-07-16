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
import com.marimo.server.domain.order.dto.Reception;
import com.marimo.server.domain.order.dto.Rsvp;
import com.marimo.server.domain.order.entity.InvitationOrderEntity;
import com.marimo.server.domain.order.entity.OrderAttachmentEntity;
import com.marimo.server.domain.order.entity.OrderEntity;
import com.marimo.server.domain.order.enums.AttachmentType;
import com.marimo.server.domain.order.repository.InvitationOrderRepository;
import com.marimo.server.domain.order.repository.OrderAttachmentRepository;
import com.marimo.server.domain.order.repository.OrderRepository;
import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.domain.product.repository.InvitationRepository;
import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    private final InvitationRepository invitationRepository;
    private final InvitationOrderRepository invitationOrderRepository;
    private final OrderAttachmentRepository orderAttachmentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createInvitationOrder(InvitationOrderRequest request) {
        if (!invitationRepository.existsById(request.invitationId())) {
            throw new BusinessException(ErrorType.NOT_FOUND_INVITATION_ERROR);
        }

        CustomerInfo customerInfo = request.customerInfo();
        InvitationCommonInfo invitationCommonInfo = request.invitationCommonInfo();
        PaperInvitationInfo paperInvitationInfo = request.paperInvitationInfo();

        boolean hasCharterBus = request.hasCharterBus();
        CharterBus charterBus = hasCharterBus ? request.charterBus() : null;

        boolean hasReception = request.hasReception();
        Reception reception = hasReception ? request.reception() : null;

        boolean hasMobileInvitation = request.hasMobileInvitation();
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
                .productId(request.invitationId())
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
                .weddingDatetime(invitationCommonInfo.weddingDatetime())
                .hasAdditionalRequest(hasAdditionalRequest)
                .requestText(additionalRequest != null ? additionalRequest.requestText() : null)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);
        Long orderId = savedOrder.getId();

        InvitationOrderEntity invitationOrderEntity = InvitationOrderEntity.builder()
                .orderId(orderId)

                // 옵션 선택 목록
                .optionList(request.optionList())

                // 혼주
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
                .receptionDatetime(reception != null ? reception.datetime() : null)

                // 모바일 청첩장
                .hasMobileInvitation(hasMobileInvitation)
                .mobileInvitationUrl(mobileInvitationInfo != null ? mobileInvitationInfo.urlPath() : null)
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
        if (hasText(paperInvitationInfo.mainImage())) {
            orderAttachmentEntities.add(
                    OrderAttachmentEntity.builder()
                            .orderId(orderId)
                            .attachmentType(AttachmentType.PAPER_INVITATION_MAIN)
                            .fileUrl(paperInvitationInfo.mainImage())
                            .build()
            );
        }

        // 모바일 청첩장 메인 이미지
        if (hasMobileInvitation && mobileInvitationInfo != null && hasText(mobileInvitationInfo.mainImage())) {
            orderAttachmentEntities.add(
                    OrderAttachmentEntity.builder()
                            .orderId(orderId)
                            .attachmentType(AttachmentType.MOBILE_INVITATION_MAIN)
                            .fileUrl(mobileInvitationInfo.mainImage())
                            .build()
            );
        }

        // 갤러리 이미지
        if (Boolean.TRUE.equals(hasGallery) && gallery != null) {
            for (String url : gallery.imageList()) {
                if (hasText(url)) {
                    orderAttachmentEntities.add(
                            OrderAttachmentEntity.builder()
                                    .orderId(orderId)
                                    .attachmentType(AttachmentType.GALLERY)
                                    .fileUrl(url)
                                    .build()
                    );
                }
            }
        }

        // 기타 요청사항 첨부파일
        if (hasAdditionalRequest && additionalRequest != null) {
            for (String url : additionalRequest.attachmentList()) {
                if (hasText(url)) {
                    orderAttachmentEntities.add(
                            OrderAttachmentEntity.builder()
                                    .orderId(orderId)
                                    .attachmentType(AttachmentType.INVITATION_REQUEST)
                                    .fileUrl(url)
                                    .build()
                    );
                }
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
}
