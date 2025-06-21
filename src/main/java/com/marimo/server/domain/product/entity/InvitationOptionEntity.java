package com.marimo.server.domain.product.entity;

import com.marimo.server.domain.product.enums.OptionType;
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
@Table(name = "invitation_option")
public class InvitationOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitation_id", nullable = false)
    private Long invitationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", length = 20, nullable = false)
    private OptionType optionType;

    @Column(name = "required", nullable = false)
    private Boolean required;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "option_detail", length = 30, nullable = false)
    private String optionDetail;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Builder
    public InvitationOptionEntity(
            Long id,
            Long invitationId,
            OptionType optionType,
            Boolean required,
            String name,
            String optionDetail,
            Integer price
    ) {
        this.id = id;
        this.invitationId = invitationId;
        this.optionType = optionType;
        this.required = required;
        this.name = name;
        this.optionDetail = optionDetail;
        this.price = price;
    }
}
