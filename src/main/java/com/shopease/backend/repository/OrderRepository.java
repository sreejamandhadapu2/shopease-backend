
package com.shopease.backend.repository;

import com.shopease.backend.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get orders belonging to a particular customer
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get all orders for admin, newest first
    List<Order> findAllByOrderByCreatedAtDesc();

    // Lock an order while processing cancellation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId);
}
