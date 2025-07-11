package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.PreVideoEntity;
import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreVideoRepository extends JpaRepository<PreVideoEntity, Long> {

    List<PreVideoEntity> findAllByOrderById();

    default PreVideoEntity findByIdOrElseThrow(Long preVideoId) {
        return findById(preVideoId).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND_PRE_VIDEO_ERROR)
        );
    }
}
