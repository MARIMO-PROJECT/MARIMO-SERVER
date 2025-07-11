package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.PreVideoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreVideoRepository extends JpaRepository<PreVideoEntity, Long> {

    List<PreVideoEntity> findAllByOrderById();
}
