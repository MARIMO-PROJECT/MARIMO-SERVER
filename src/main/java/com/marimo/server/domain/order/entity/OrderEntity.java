package com.marimo.server.domain.order.entity;

import com.marimo.server.domain.product.enums.ProductType;
import com.marimo.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class OrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 20, nullable = false)
    private ProductType productType;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "customer_name", length = 30, nullable = false)
    private String customerName;

    @Column(name = "zone_code", length = 5, nullable = false)
    private String zoneCode;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "detail_address", length = 100)
    private String detailAddress;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "has_groom_christian_name", nullable = false)
    private Boolean hasGroomChristianName;

    @Column(name = "groom_name", length = 30, nullable = false)
    private String groomName;

    @Column(name = "groom_christian_name", length = 30)
    private String groomChristianName;

    @Column(name = "has_bride_christian_name", nullable = false)
    private Boolean hasBrideChristianName;

    @Column(name = "bride_name", length = 30, nullable = false)
    private String brideName;

    @Column(name = "bride_christian_name", length = 30)
    private String brideChristianName;

    @Column(name = "wedding_datetime", nullable = false)
    private LocalDateTime weddingDatetime;

    @Column(name = "has_additional_request", nullable = false)
    private Boolean hasAdditionalRequest;

    @Column(name = "request_text", length = 500)
    private String requestText;

    @Builder
    public OrderEntity(
            Long id,
            ProductType productType,
            Long productId,
            String code,
            String customerName,
            String zoneCode,
            String address,
            String detailAddress,
            String phoneNumber,
            String email,
            Boolean hasGroomChristianName,
            String groomName,
            String groomChristianName,
            Boolean hasBrideChristianName,
            String brideName,
            String brideChristianName,
            LocalDateTime weddingDatetime,
            Boolean hasAdditionalRequest,
            String requestText
    ) {
        this.id = id;
        this.productType = productType;
        this.productId = productId;
        this.code = code;
        this.customerName = customerName;
        this.zoneCode = zoneCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.hasGroomChristianName = hasGroomChristianName;
        this.groomName = groomName;
        this.groomChristianName = groomChristianName;
        this.hasBrideChristianName = hasBrideChristianName;
        this.brideName = brideName;
        this.brideChristianName = brideChristianName;
        this.weddingDatetime = weddingDatetime;
        this.hasAdditionalRequest = hasAdditionalRequest;
        this.requestText = requestText;
    }
}
