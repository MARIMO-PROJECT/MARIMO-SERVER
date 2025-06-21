package com.marimo.server.domain.product.entity;

import com.marimo.server.domain.product.enums.ImageType;
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
@Table(name = "product_image")
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 30, nullable = false)
    private ImageType imageType;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_url", columnDefinition = "text", nullable = false)
    private String imageUrl;

    @Builder
    public ProductImageEntity(
            Long id,
            ImageType imageType,
            Long productId,
            String imageUrl
    ) {
        this.id = id;
        this.imageType = imageType;
        this.productId = productId;
        this.imageUrl = imageUrl;
    }
}
