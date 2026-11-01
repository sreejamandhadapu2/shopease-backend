
package com.shopease.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopease.backend.entity.OrderItem;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
}
