package com.shopease.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PaymentProcessRequest {

    @NotBlank(message = "Payment result is required")
    @Pattern(
        regexp = "SUCCESS|FAILED",
        message = "Payment result must be SUCCESS or FAILED"
    )
    private String paymentResult;

    public String getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(String paymentResult) {
        this.paymentResult = paymentResult;
    }
}