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
@Table(name = "invitation")
public class InvitationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "has_bundle", nullable = false)
    private Boolean hasBundle;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @Builder
    public InvitationEntity(
            Long id,
            Boolean hasBundle,
            String name,
            Integer discountRate,
            String description
    ) {
        this.id = id;
        this.hasBundle = hasBundle;
        this.name = name;
        this.discountRate = discountRate;
        this.description = description;
    }
}
