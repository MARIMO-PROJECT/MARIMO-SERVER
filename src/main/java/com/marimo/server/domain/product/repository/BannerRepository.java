package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.BannerEntity;
import com.marimo.server.domain.product.enums.ProductType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<BannerEntity, Long> {

    List<BannerEntity> findAllByProductTypeAndActiveTrueOrderByIdDesc(final ProductType productType);
}
