package com.shopease.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailsViewResponse {

    private Long orderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    // Delivery address details
    private String deliveryFullName;
    private String deliveryPhoneNumber;
    private String deliveryAddressLine;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPincode;
    private String deliveryCountry;

    private List<OrderDetailsResponse> items;

    public OrderDetailsViewResponse(
            Long orderId,
            BigDecimal totalAmount,
            String status,
            LocalDateTime createdAt,
            String deliveryFullName,
            String deliveryPhoneNumber,
            String deliveryAddressLine,
            String deliveryCity,
            String deliveryState,
            String deliveryPincode,
            String deliveryCountry,
            List<OrderDetailsResponse> items) {

        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;

        this.deliveryFullName = deliveryFullName;
        this.deliveryPhoneNumber = deliveryPhoneNumber;
        this.deliveryAddressLine = deliveryAddressLine;
        this.deliveryCity = deliveryCity;
        this.deliveryState = deliveryState;
        this.deliveryPincode = deliveryPincode;
        this.deliveryCountry = deliveryCountry;

        this.items = items;
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

    public String getDeliveryFullName() {
        return deliveryFullName;
    }

    public String getDeliveryPhoneNumber() {
        return deliveryPhoneNumber;
    }

    public String getDeliveryAddressLine() {
        return deliveryAddressLine;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public String getDeliveryPincode() {
        return deliveryPincode;
    }

    public String getDeliveryCountry() {
        return deliveryCountry;
    }

    public List<OrderDetailsResponse> getItems() {
        return items;
    }
}