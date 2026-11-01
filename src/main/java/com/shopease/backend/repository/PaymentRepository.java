
package com.shopease.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopease.backend.entity.Payment;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(
            Long orderId);

    Optional<Payment> findByStripePaymentIntentId(
            String stripePaymentIntentId);

    List<Payment> findByOrderUserIdOrderByCreatedAtDesc(
            Long userId);
}
