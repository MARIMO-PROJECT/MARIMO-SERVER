package com.marimo.server.domain.order.repository;

import com.marimo.server.domain.order.entity.OrderAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAttachmentRepository extends JpaRepository<OrderAttachmentEntity, Long> {

}
