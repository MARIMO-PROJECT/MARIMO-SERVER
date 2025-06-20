package com.marimo.server.domain.product.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "banner")
public class BannerEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 20, nullable = false)
    private ProductType productType;

    @Column(name = "file_url", columnDefinition = "text", nullable = false)
    private String fileUrl;

    @Column(name = "cta_url", columnDefinition = "text")
    private String ctaUrl;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Builder
    public BannerEntity(
            Long id,
            ProductType productType,
            String fileUrl,
            String ctaUrl,
            Boolean active
    ) {
        this.id = id;
        this.productType = productType;
        this.fileUrl = fileUrl;
        this.ctaUrl = ctaUrl;
        this.active = active;
    }
}
