
package com.shopease.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopease.backend.dto.AdminOrderResponse;
import com.shopease.backend.dto.OrderHistoryResponse;
import com.shopease.backend.dto.UpdateOrderStatusRequest;
import com.shopease.backend.entity.Order;
import com.shopease.backend.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Admin: View all customer orders
    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders() {

        List<AdminOrderResponse> orders =
                orderService.getAllOrdersForAdmin();

        return ResponseEntity.ok(orders);
    }

    // Admin: Update order status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderHistoryResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Order updatedOrder = orderService.updateOrderStatus(
                orderId,
                request.getStatus()
        );

        OrderHistoryResponse response = new OrderHistoryResponse(
                updatedOrder.getId(),
                updatedOrder.getTotalAmount(),
                updatedOrder.getStatus(),
                updatedOrder.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
