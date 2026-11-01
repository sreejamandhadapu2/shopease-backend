
package com.shopease.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import com.shopease.backend.dto.PaymentResponse;
import com.shopease.backend.dto.PaymentProcessRequest;
import com.shopease.backend.entity.Payment;
import com.shopease.backend.entity.PaymentStatus;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(
            PaymentService paymentService,
            UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        Payment payment =
                paymentService.createPayment(userId, orderId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PaymentResponse(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        Payment payment =
                paymentService.getPayment(userId, paymentId);

        return ResponseEntity.ok(new PaymentResponse(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        List<PaymentResponse> payments =
                paymentService.getPaymentHistory(userId)
                        .stream()
                        .map(PaymentResponse::new)
                        .toList();

        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentProcessRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        // Verify that this payment belongs to the logged-in user
        paymentService.getPayment(userId, paymentId);

        if ("SUCCESS".equals(request.getPaymentResult())) {
            paymentService.markPaymentSuccessful(paymentId);
        } else {
            paymentService.markPaymentFailed(paymentId);
        }

        Payment updatedPayment =
                paymentService.getPayment(userId, paymentId);

        return ResponseEntity.ok(
                new PaymentResponse(updatedPayment));
    }

    private Long getUserId(Authentication authentication) {
        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"))
                .getId();
    }
}
