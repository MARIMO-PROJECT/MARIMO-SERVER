package com.marimo.server.domain.order.entity;

import com.marimo.server.domain.order.enums.AttachmentType;
import com.marimo.server.domain.order.enums.FileType;
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
@Table(name = "order_attachment")
public class OrderAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 30, nullable = false)
    private AttachmentType attachmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(name = "file_url", columnDefinition = "text", nullable = false)
    private String fileUrl;

    @Builder
    public OrderAttachmentEntity(
            Long id,
            Long orderId,
            AttachmentType attachmentType,
            FileType fileType,
            String fileUrl
    ) {
        this.id = id;
        this.orderId = orderId;
        this.attachmentType = attachmentType;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
    }
}
