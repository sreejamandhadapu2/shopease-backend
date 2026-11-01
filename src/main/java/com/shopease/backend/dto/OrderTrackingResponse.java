package com.shopease.backend.dto;

import java.time.LocalDateTime;

public class OrderTrackingResponse {

    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;

    public OrderTrackingResponse(
            String previousStatus,
            String newStatus,
            LocalDateTime changedAt) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}