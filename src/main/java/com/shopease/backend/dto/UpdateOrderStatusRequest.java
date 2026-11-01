
package com.shopease.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateOrderStatusRequest {

    @NotBlank(message = "Order status is required")
    @Pattern(
        regexp = "PROCESSING|SHIPPED|DELIVERED",
        message = "Status must be PROCESSING, SHIPPED or DELIVERED"
    )
    private String status;

    public UpdateOrderStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
