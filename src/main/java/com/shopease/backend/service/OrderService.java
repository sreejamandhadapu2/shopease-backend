package com.shopease.backend.service;

import com.shopease.backend.dto.AdminOrderResponse;
import com.shopease.backend.dto.CartItemResponse;
import com.shopease.backend.dto.CartResponse;
import com.shopease.backend.dto.OrderDetailsResponse;
import com.shopease.backend.dto.OrderDetailsViewResponse;
import com.shopease.backend.dto.OrderHistoryResponse;
import com.shopease.backend.dto.OrderTrackingResponse;
import com.shopease.backend.entity.Address;
import com.shopease.backend.entity.Cart;
import com.shopease.backend.entity.Order;
import com.shopease.backend.entity.OrderItem;
import com.shopease.backend.entity.OrderStatusHistory;
import com.shopease.backend.entity.Product;
import com.shopease.backend.entity.User;
import com.shopease.backend.repository.AddressRepository;
import com.shopease.backend.repository.CartRepository;
import com.shopease.backend.repository.OrderItemRepository;
import com.shopease.backend.repository.OrderRepository;
import com.shopease.backend.repository.OrderStatusHistoryRepository;
import com.shopease.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final AddressRepository addressRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            UserRepository userRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            AddressRepository addressRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Order createOrder(Long userId, Long addressId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
        Address address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery address not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot create an order from an empty cart");
        }

        // Validate stock for every product before deducting any stock
        for (var cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();

            if (product.getStock() < requestedQuantity) {
                throw new IllegalStateException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }
        }

        BigDecimal total = BigDecimal.ZERO;

        // Deduct stock and calculate the order total
        for (var cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            product.setStock(product.getStock() - quantity);

            total = total.add(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(quantity))
            );
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total);
        order.setStatus("PENDING");

        // Save delivery address snapshot
        order.setDeliveryFullName(address.getFullName());
        order.setDeliveryPhoneNumber(address.getPhoneNumber());
        order.setDeliveryAddressLine(address.getAddressLine());
        order.setDeliveryCity(address.getCity());
        order.setDeliveryState(address.getState());
        order.setDeliveryPincode(address.getPincode());
        order.setDeliveryCountry(address.getCountry());

        Order savedOrder = orderRepository.save(order);

        for (var cartItem : cart.getItems()) {

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());

            orderItemRepository.save(orderItem);
        }

        cart.getItems().clear();

        return savedOrder;
    }
    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getOrderHistory(Long userId) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(order -> new OrderHistoryResponse(
                        order.getId(),
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public OrderDetailsViewResponse getOrderDetails(
            Long userId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        List<OrderDetailsResponse> items =
                orderItemRepository.findAll().stream()
                        .filter(item ->
                                item.getOrder().getId().equals(orderId))
                        .map(item -> new OrderDetailsResponse(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getPrice().multiply(
                                        BigDecimal.valueOf(item.getQuantity()))
                        ))
                        .toList();

        return new OrderDetailsViewResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),

                order.getDeliveryFullName(),
                order.getDeliveryPhoneNumber(),
                order.getDeliveryAddressLine(),
                order.getDeliveryCity(),
                order.getDeliveryState(),
                order.getDeliveryPincode(),
                order.getDeliveryCountry(),

                items
        );
    }

    @Transactional
    public Order cancelOrder(Long userId, Long orderId) {

        Order order = orderRepository.findByIdForUpdate(orderId)
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Only pending orders can be cancelled");
        }

        // Store the previous status
        String previousStatus = order.getStatus();

        // Retrieve all products belonging to this order
        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(orderId);

        // Restore stock for every ordered product
        for (OrderItem item : orderItems) {

            Product product = item.getProduct();

            product.setStock(
                    product.getStock() + item.getQuantity()
            );
        }

        // Update order status
        order.setStatus("CANCELLED");

        // Record cancellation in order tracking history
        OrderStatusHistory history = new OrderStatusHistory();

        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus("CANCELLED");

        orderStatusHistoryRepository.save(history);

        return order;
    }
@Transactional(readOnly = true)
public List<AdminOrderResponse> getAllOrdersForAdmin() {

    return orderRepository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .map(order -> new AdminOrderResponse(
                    order.getId(),
                    order.getUser().getId(),
                    order.getUser().getName(),
                    order.getUser().getEmail(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getCreatedAt()
            ))
            .collect(Collectors.toList());
}

@Transactional
public Order updateOrderStatus(Long orderId, String newStatus) {

    Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    String currentStatus = order.getStatus();

    boolean validTransition = false;

    if ("CONFIRMED".equals(currentStatus)
            && "PROCESSING".equals(newStatus)) {
        validTransition = true;

    } else if ("PROCESSING".equals(currentStatus)
            && "SHIPPED".equals(newStatus)) {
        validTransition = true;

    } else if ("SHIPPED".equals(currentStatus)
            && "DELIVERED".equals(newStatus)) {
        validTransition = true;
    }

    if (!validTransition) {
        throw new IllegalStateException(
                "Invalid order status transition from "
                        + currentStatus + " to " + newStatus);
    }

    // Update the order's current status
    order.setStatus(newStatus);

    // Create an order status history record
    OrderStatusHistory history = new OrderStatusHistory();

    history.setOrder(order);
    history.setPreviousStatus(currentStatus);
    history.setNewStatus(newStatus);

    // Save the history record
    orderStatusHistoryRepository.save(history);

    return order;
}
@Transactional(readOnly = true)
public List<OrderTrackingResponse> getOrderTracking(
        Long userId, Long orderId) {

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    if (!order.getUser().getId().equals(userId)) {
        throw new org.springframework.security.access.AccessDeniedException(
                "You are not authorized to view this order");
    }

    return orderStatusHistoryRepository
            .findByOrderIdOrderByChangedAtAsc(orderId)
            .stream()
            .map(history -> new OrderTrackingResponse(
                    history.getPreviousStatus(),
                    history.getNewStatus(),
                    history.getChangedAt()))
            .toList();
}
	
}