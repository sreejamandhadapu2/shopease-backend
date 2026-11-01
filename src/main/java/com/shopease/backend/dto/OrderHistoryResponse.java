package com.shopease.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderHistoryResponse {

    private Long orderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public OrderHistoryResponse(
            Long orderId,
            BigDecimal totalAmount,
            String status,
            LocalDateTime createdAt) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}