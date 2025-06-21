package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.InvitationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    List<InvitationEntity> findAllByOrderById();
}
