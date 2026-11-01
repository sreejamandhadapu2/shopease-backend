
package com.shopease.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopease.backend.entity.Order;
import com.shopease.backend.entity.OrderStatusHistory;
import com.shopease.backend.entity.Payment;
import com.shopease.backend.entity.PaymentStatus;
import com.shopease.backend.repository.OrderRepository;
import com.shopease.backend.repository.OrderStatusHistoryRepository;
import com.shopease.backend.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }


@Transactional
public Payment createPayment(Long userId, Long orderId) {

    Order order = orderRepository.findById(orderId)
            .filter(o -> o.getUser().getId().equals(userId))
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    if (!"PENDING".equals(order.getStatus())) {
        throw new IllegalStateException(
                "Order is not eligible for payment");
    }

    // Check the latest payment attempt
    Payment latestPayment = paymentRepository
            .findTopByOrderIdOrderByCreatedAtDesc(orderId)
            .orElse(null);

    // Allow a new attempt only if the previous one failed
    if (latestPayment != null
            && latestPayment.getStatus() != PaymentStatus.FAILED) {

        throw new IllegalStateException(
                "A new payment attempt is not allowed. "
                + "Latest payment status: "
                + latestPayment.getStatus());
    }

    Payment payment = new Payment();
    payment.setOrder(order);
    payment.setAmount(order.getTotalAmount());
    payment.setStatus(PaymentStatus.CREATED);

    return paymentRepository.save(payment);
}


@Transactional(readOnly = true)
public Payment getPayment(Long userId, Long paymentId) {

    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() ->
                    new RuntimeException("Payment not found"));

    if (!payment.getOrder().getUser().getId().equals(userId)) {
        throw new RuntimeException("Payment not found");
    }

    return payment;
}

@Transactional
public void markPaymentSuccessful(Long paymentId) {

    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() ->
                    new RuntimeException("Payment not found"));

    // Prevent duplicate success processing
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
        return;
    }

    if (payment.getStatus() == PaymentStatus.FAILED) {
        throw new IllegalStateException(
                "Failed payment cannot be marked successful");
    }

    Order order = payment.getOrder();

    if (!"PENDING".equals(order.getStatus())) {
        throw new IllegalStateException(
                "Payment cannot be completed because the order is not pending");
    }

    // Store the previous order status
    String previousStatus = order.getStatus();

    // Update payment and order
    payment.setStatus(PaymentStatus.SUCCESS);
    order.setStatus("CONFIRMED");

    // Record the order status transition
    OrderStatusHistory history = new OrderStatusHistory();

    history.setOrder(order);
    history.setPreviousStatus(previousStatus);
    history.setNewStatus("CONFIRMED");

    orderStatusHistoryRepository.save(history);
}

@Transactional
public void markPaymentFailed(Long paymentId) {

    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() ->
                    new RuntimeException("Payment not found"));

    // Never downgrade a successful payment
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
        return;
    }

    payment.setStatus(PaymentStatus.FAILED);
}
@Transactional(readOnly = true)
public List<Payment> getPaymentHistory(Long userId) {

    return paymentRepository
            .findByOrderUserIdOrderByCreatedAtDesc(userId);
}

}
