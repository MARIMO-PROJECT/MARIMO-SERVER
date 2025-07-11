package com.marimo.server.domain.product.entity;

import com.marimo.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "pre_video")
public class PreVideoEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "sample_video_url", columnDefinition = "text", nullable = false)
    private String sampleVideoUrl;

    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @Builder
    public PreVideoEntity(
            Long id,
            String name,
            Integer discountRate,
            Integer price,
            String sampleVideoUrl,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.discountRate = discountRate;
        this.price = price;
        this.sampleVideoUrl = sampleVideoUrl;
        this.description = description;
    }
}
