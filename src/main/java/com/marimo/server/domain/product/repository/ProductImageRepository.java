package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.ProductImageEntity;
import com.marimo.server.domain.product.enums.ImageType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findAllByImageTypeOrderByProductIdAscIdAsc(final ImageType imageType);
}
