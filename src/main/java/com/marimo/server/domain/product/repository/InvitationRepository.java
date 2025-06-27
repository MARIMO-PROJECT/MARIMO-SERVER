package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.InvitationEntity;
import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    List<InvitationEntity> findAllByOrderById();

    default InvitationEntity findByIdOrElseThrow(Long invitationId) {
        return findById(invitationId).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND_INVITATION_ERROR)
        );
    }
}
