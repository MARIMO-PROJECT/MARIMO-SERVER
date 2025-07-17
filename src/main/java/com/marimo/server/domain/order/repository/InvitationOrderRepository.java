package com.marimo.server.domain.order.repository;

import com.marimo.server.domain.order.entity.InvitationOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationOrderRepository extends JpaRepository<InvitationOrderEntity, Long> {

}
