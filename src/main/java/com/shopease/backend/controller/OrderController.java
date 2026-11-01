package com.shopease.backend.controller;

import com.shopease.backend.dto.OrderDetailsViewResponse;
import com.shopease.backend.dto.OrderHistoryResponse;
import com.shopease.backend.dto.OrderResponse;
import com.shopease.backend.dto.OrderTrackingResponse;
import com.shopease.backend.entity.Order;
import com.shopease.backend.entity.User;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.service.OrderService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(
            OrderService orderService,
            UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId(
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return user.getId();
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            Authentication authentication,
            @RequestParam Long addressId) {

        Long userId = getAuthenticatedUserId(authentication);

        Order order = orderService.createOrder(userId, addressId);

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        List<OrderHistoryResponse> orders =
                orderService.getOrderHistory(userId);

        return ResponseEntity.ok(orders);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsViewResponse> getOrderDetails(
            Authentication authentication,
            @PathVariable Long orderId) {

        Long userId = getAuthenticatedUserId(authentication);

        OrderDetailsViewResponse response =
                orderService.getOrderDetails(userId, orderId);

        return ResponseEntity.ok(response);
    }

@PutMapping("/{orderId}/cancel")
public ResponseEntity<OrderResponse> cancelOrder(
        Authentication authentication,
        @PathVariable Long orderId) {

    Long userId = getAuthenticatedUserId(authentication);

    Order order = orderService.cancelOrder(userId, orderId);

    OrderResponse response = new OrderResponse(
            order.getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt()
    );

    return ResponseEntity.ok(response);
}
@GetMapping("/{orderId}/tracking")
public ResponseEntity<List<OrderTrackingResponse>> getOrderTracking(
        @PathVariable Long orderId,
        Authentication authentication) {

    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    List<OrderTrackingResponse> tracking =
            orderService.getOrderTracking(user.getId(), orderId);

    return ResponseEntity.ok(tracking);
}

}