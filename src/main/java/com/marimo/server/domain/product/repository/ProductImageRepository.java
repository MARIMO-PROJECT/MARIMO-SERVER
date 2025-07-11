package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.ProductImageEntity;
import com.marimo.server.domain.product.enums.ImageType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findAllByImageTypeOrderById(final ImageType imageType);

    List<ProductImageEntity> findAllByProductIdOrderById(final Long productId);

    @Query(value = """
            SELECT p.image_url
            FROM   product_image p
            WHERE  p.image_type = :imageType
              AND  p.product_id = :productId
            ORDER BY p.id
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findFirstImageUrlByImageTypeAndProductId(
            @Param("imageType") String imageType,
            @Param("productId") Long productId
    );
}
