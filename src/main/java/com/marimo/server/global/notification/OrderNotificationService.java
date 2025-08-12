package com.marimo.server.global.notification;

import com.marimo.server.domain.order.dto.request.InvitationOrderRequest;
import com.marimo.server.domain.order.dto.request.PreVideoOrderRequest;
import com.marimo.server.domain.order.dto.request.SelectedOption;
import com.marimo.server.domain.order.dto.response.OrderResponse;
import com.marimo.server.domain.product.entity.InvitationEntity;
import com.marimo.server.domain.product.entity.InvitationOptionEntity;
import com.marimo.server.domain.product.entity.PreVideoEntity;
import com.marimo.server.domain.product.repository.InvitationOptionRepository;
import com.marimo.server.domain.product.repository.InvitationRepository;
import com.marimo.server.domain.product.repository.PreVideoRepository;
import com.marimo.server.global.event.InvitationOrderCreatedEvent;
import com.marimo.server.global.event.PreVideoOrderCreatedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

    private static final Logger ORDER_LOGGER = LoggerFactory.getLogger("ORDER_NOTIFICATION");

    private final InvitationRepository invitationRepository;
    private final InvitationOptionRepository invitationOptionRepository;
    private final PreVideoRepository preVideoRepository;

    @EventListener
    @Async("orderNotificationTaskExecutor")
    public void handleInvitationOrderCreated(InvitationOrderCreatedEvent event) {
        try {
            InvitationOrderRequest request = event.getRequest();
            OrderResponse response = event.getResponse();

            // 1. 상품명 조회
            InvitationEntity invitation = invitationRepository.findById(request.invitationId())
                    .orElse(null);
            String productName = invitation != null ? invitation.getName() : "청첩장";

            // 2. 고객 정보
            String customerName = request.customerInfo().name();
            String phoneNumber = request.customerInfo().phoneNumber();
            String address = buildAddress(request.customerInfo().address(), request.customerInfo().detailAddress());

            // 3. 총액 계산 (옵션별 가격 * 수량)
            long totalAmount = calculateInvitationTotalAmount(request.optionList());

            // 4. 주문번호
            String orderCode = response.orderCode();

            String message = String.format("🎉 새로운 주문이 접수되었습니다 - [주문 성공] 청첩장 주문\n" +
                            "🔖 주문번호: %s\n" +
                            "🎨 상품명: %s\n" +
                            "📋 주문자: %s\n" +
                            "📞 연락처: %s\n" +
                            "📍 배송 주소: %s\n" +
                            "💰 주문 총액: %,d원",
                    orderCode,
                    productName,
                    customerName,
                    phoneNumber,
                    address,
                    totalAmount
            );

            ORDER_LOGGER.info(message);
            log.info("청첩장 주문 알림 전송됨 - 고객: {}, 주문번호: {}", customerName, orderCode);

        } catch (Exception e) {
            log.error("청첩장 주문 알림 처리 중 오류 발생", e);
        }
    }

    @EventListener
    @Async("orderNotificationTaskExecutor")
    public void handlePreVideoOrderCreated(PreVideoOrderCreatedEvent event) {
        try {
            PreVideoOrderRequest request = event.getRequest();
            OrderResponse response = event.getResponse();

            // 1. 상품명과 가격 조회
            PreVideoEntity preVideo = preVideoRepository.findById(request.preVideoId())
                    .orElse(null);
            String productName = preVideo != null ? preVideo.getName() : "식전영상";
            long totalAmount = preVideo != null ? preVideo.getPrice() : 0;

            // 2. 고객 정보
            String customerName = request.customerInfo().name();
            String phoneNumber = request.customerInfo().phoneNumber();
            String address = buildAddress(request.customerInfo().address(), request.customerInfo().detailAddress());

            // 3. 주문번호
            String orderCode = response.orderCode();

            String message = String.format("🎉 새로운 주문이 접수되었습니다 - [주문 성공] 식전영상 주문\n" +
                            "🔖 주문번호: %s\n" +
                            "🎬 상품명: %s\n" +
                            "📋 주문자: %s\n" +
                            "📞 연락처: %s\n" +
                            "📍 배송 주소: %s\n" +
                            "💰 주문 총액: %,d원",
                    orderCode,
                    productName,
                    customerName,
                    phoneNumber,
                    address,
                    totalAmount
            );

            ORDER_LOGGER.info(message);
            log.info("식전영상 주문 알림 전송됨 - 고객: {}, 주문번호: {}", customerName, orderCode);

        } catch (Exception e) {
            log.error("식전영상 주문 알림 처리 중 오류 발생", e);
        }
    }

    private long calculateInvitationTotalAmount(List<SelectedOption> optionList) {
        long totalAmount = 0;

        for (SelectedOption selectedOption : optionList) {
            InvitationOptionEntity option = invitationOptionRepository.findById(selectedOption.optionId())
                    .orElse(null);

            if (option != null) {
                totalAmount += (long) option.getPrice() * selectedOption.quantity();
            }
        }

        return totalAmount;
    }

    private String buildAddress(String address, String detailAddress) {
        if (StringUtils.hasText(detailAddress)) {
            return address + " " + detailAddress;
        }
        return address;
    }
}
