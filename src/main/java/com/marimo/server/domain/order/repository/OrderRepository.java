package com.marimo.server.domain.order.repository;

import com.marimo.server.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByCode(String code);
}
