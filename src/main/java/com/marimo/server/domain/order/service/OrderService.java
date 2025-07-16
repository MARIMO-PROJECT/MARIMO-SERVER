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

        CustomerInfo ci = request.customerInfo();
        InvitationCommonInfo ici = request.invitationCommonInfo();
        PaperInvitationInfo pii = request.paperInvitationInfo();

        boolean hasCharterBus = request.hasCharterBus();
        CharterBus cb = hasCharterBus ? request.charterBus() : null;

        boolean hasReception = request.hasReception();
        Reception reception = hasReception ? request.reception() : null;

        boolean hasMobileInvitation = request.hasMobileInvitation();
        MobileInvitationInfo mii = hasMobileInvitation ? request.mobileInvitationInfo() : null;

        Boolean hasGallery = request.hasGallery();
        Gallery gallery = Boolean.TRUE.equals(hasGallery) ? request.gallery() : null;

        Boolean hasContactOption = request.hasContactOption();
        ContactOption co = Boolean.TRUE.equals(hasContactOption) ? request.contactOption() : null;

        Boolean hasGiftAccount = request.hasGiftAccount();
        GiftAccount ga = Boolean.TRUE.equals(hasGiftAccount) ? request.giftAccount() : null;

        Boolean hasCalendar = request.hasCalendar();
        Boolean hasMapNavigation = request.hasMapNavigation();

        Boolean hasGuestbook = request.hasGuestbook();
        Guestbook guestbook = Boolean.TRUE.equals(hasGuestbook) ? request.guestbook() : null;

        Boolean hasRsvp = request.hasRsvp();
        Rsvp rsvp = Boolean.TRUE.equals(hasRsvp) ? request.rsvp() : null;

        boolean hasAdditionalRequest = request.hasAdditionalRequest();
        AdditionalRequestInfo ari = hasAdditionalRequest ? request.additionalRequest() : null;

        OrderEntity orderEntity = OrderEntity.builder()
                .productType(ProductType.INVITATION)
                .productId(request.invitationId())
                .code(generateUniqueOrderCode())
                .customerName(ci.name())
                .zoneCode(ci.zoneCode())
                .address(ci.address())
                .detailAddress(ci.detailAddress())
                .phoneNumber(ci.phoneNumber())
                .email(ci.email())
                .hasGroomChristianName(ici.hasGroomChristianName())
                .groomName(ici.groomName())
                .groomChristianName(ici.groomChristianName())
                .hasBrideChristianName(ici.hasBrideChristianName())
                .brideName(ici.brideName())
                .brideChristianName(ici.brideChristianName())
                .weddingDatetime(ici.weddingDatetime())
                .hasAdditionalRequest(hasAdditionalRequest)
                .requestText(ari != null ? ari.requestText() : null)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);
        Long orderId = savedOrder.getId();

        InvitationOrderEntity invitationOrderEntity = InvitationOrderEntity.builder()
                .orderId(orderId)

                // 옵션 선택 목록
                .optionList(request.optionList())

                // 혼주
                .groomFatherDeceased(ici.groomFatherDeceased())
                .hasGroomFatherChristianName(ici.hasGroomFatherChristianName())
                .groomFatherName(ici.groomFatherDeceased() ? null : ici.groomFatherName())
                .groomFatherChristianName(ici.hasGroomFatherChristianName() ? ici.groomFatherChristianName() : null)

                .groomMotherDeceased(ici.groomMotherDeceased())
                .hasGroomMotherChristianName(ici.hasGroomMotherChristianName())
                .groomMotherName(ici.groomMotherDeceased() ? null : ici.groomMotherName())
                .groomMotherChristianName(ici.hasGroomMotherChristianName() ? ici.groomMotherChristianName() : null)

                .brideFatherDeceased(ici.brideFatherDeceased())
                .hasBrideFatherChristianName(ici.hasBrideFatherChristianName())
                .brideFatherName(ici.brideFatherDeceased() ? null : ici.brideFatherName())
                .brideFatherChristianName(ici.hasBrideFatherChristianName() ? ici.brideFatherChristianName() : null)

                .brideMotherDeceased(ici.brideMotherDeceased())
                .hasBrideMotherChristianName(ici.hasBrideMotherChristianName())
                .brideMotherName(ici.brideMotherDeceased() ? null : ici.brideMotherName())
                .brideMotherChristianName(ici.hasBrideMotherChristianName() ? ici.brideMotherChristianName() : null)

                // 예식장
                .weddingVenueZoneCode(ici.weddingVenueZoneCode())
                .weddingVenueAddress(ici.weddingVenueAddress())
                .weddingVenueDetailAddress(ici.weddingVenueDetailAddress())

                // 종이 청첩장
                .paperInvitationMessage(pii.message())

                // 전세버스
                .hasCharterBus(hasCharterBus)
                .busStopLocation(cb != null ? cb.busStopLocation() : null)
                .busStopTimeList(cb != null ? cb.busStopTimeList() : null)

                // 피로연
                .hasReception(hasReception)
                .receptionAddress(reception != null ? reception.address() : null)
                .receptionDatetime(reception != null ? reception.datetime() : null)

                // 모바일 청첩장
                .hasMobileInvitation(hasMobileInvitation)
                .mobileInvitationUrl(mii != null ? mii.urlPath() : null)
                .mobileInvitationMessage(mii != null ? mii.message() : null)

                // 갤러리
                .hasGallery(hasGallery)

                // 전화걸기
                .hasContactOption(hasContactOption)
                .groomFatherPhoneNumber(co != null ? co.groomFatherPhoneNumber() : null)
                .groomMotherPhoneNumber(co != null ? co.groomMotherPhoneNumber() : null)
                .groomPhoneNumber(co != null ? co.groomPhoneNumber() : null)
                .brideFatherPhoneNumber(co != null ? co.brideFatherPhoneNumber() : null)
                .brideMotherPhoneNumber(co != null ? co.brideMotherPhoneNumber() : null)
                .bridePhoneNumber(co != null ? co.bridePhoneNumber() : null)

                // 축의금계좌
                .hasGiftAccount(hasGiftAccount)
                .groomGiftAccountList(ga != null ? ga.groomGiftAccountList() : null)
                .brideGiftAccountList(ga != null ? ga.brideGiftAccountList() : null)

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
        if (hasText(pii.mainImage())) {
            orderAttachmentEntities.add(
                    OrderAttachmentEntity.builder()
                            .orderId(orderId)
                            .attachmentType(AttachmentType.PAPER_INVITATION_MAIN)
                            .fileUrl(pii.mainImage())
                            .build()
            );
        }

        // 모바일 청첩장 메인 이미지
        if (hasMobileInvitation && mii != null && hasText(mii.mainImage())) {
            orderAttachmentEntities.add(
                    OrderAttachmentEntity.builder()
                            .orderId(orderId)
                            .attachmentType(AttachmentType.MOBILE_INVITATION_MAIN)
                            .fileUrl(mii.mainImage())
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
        if (hasAdditionalRequest && ari != null) {
            for (String url : ari.attachmentList()) {
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
