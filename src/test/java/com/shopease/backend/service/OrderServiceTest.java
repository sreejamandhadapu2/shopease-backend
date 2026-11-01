
package com.shopease.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shopease.backend.entity.Address;
import com.shopease.backend.entity.Cart;
import com.shopease.backend.entity.CartItem;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderService orderService;


    // TEST 1: Successfully create an order

    @Test
    void shouldSuccessfullyCreateOrder() {

        Long userId = 1L;
        Long addressId = 1L;

        User user = mock(User.class);
        Address address = mock(Address.class);
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);
        Order savedOrder = mock(Order.class);

        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(address));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getItems()).thenReturn(items);

        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(2);

        when(product.getStock()).thenReturn(10);
        when(product.getPrice()).thenReturn(new BigDecimal("1000.00"));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        Order response = orderService.createOrder(userId, addressId);

        assertNotNull(response);

        verify(product).setStock(8);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));

        assertTrue(items.isEmpty());
    }


    // TEST 2: Reject checkout when user does not exist

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(999L, 1L)
        );

        assertEquals("User not found", exception.getMessage());

        verifyNoInteractions(
                addressRepository,
                cartRepository,
                orderRepository
        );
    }


    // TEST 3: Reject address that does not belong to user

    @Test
    void shouldThrowExceptionWhenDeliveryAddressDoesNotExist() {

        Long userId = 1L;
        Long addressId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mock(User.class)));

        when(addressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(userId, addressId)
        );

        assertEquals(
                "Delivery address not found",
                exception.getMessage()
        );

        verifyNoInteractions(cartRepository, orderRepository);
    }


    // TEST 4: Reject checkout when cart does not exist

    @Test
    void shouldThrowExceptionWhenCartDoesNotExist() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mock(User.class)));

        when(addressRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(mock(Address.class)));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(userId, 1L)
        );

        assertEquals("Cart not found", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }


    // TEST 5: Reject checkout when cart is empty

    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {

        Long userId = 1L;

        Cart cart = mock(Cart.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mock(User.class)));

        when(addressRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(mock(Address.class)));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getItems()).thenReturn(new ArrayList<>());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrder(userId, 1L)
        );

        assertEquals(
                "Cannot create an order from an empty cart",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(Order.class));
    }


    // TEST 6: Reject checkout when stock is insufficient

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        Long userId = 1L;

        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mock(User.class)));

        when(addressRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(mock(Address.class)));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getItems()).thenReturn(items);

        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(5);

        when(product.getStock()).thenReturn(2);
        when(product.getName()).thenReturn("Bluetooth Speaker");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrder(userId, 1L)
        );

        assertEquals(
                "Insufficient stock for product: Bluetooth Speaker",
                exception.getMessage()
        );

        verify(product, never()).setStock(anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }


    // TEST 7: Successfully retrieve order history

    @Test
    void shouldSuccessfullyRetrieveOrderHistory() {

        Long userId = 1L;

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(order1, order2));

        List<?> response = orderService.getOrderHistory(userId);

        assertNotNull(response);
        assertEquals(2, response.size());

        verify(orderRepository)
                .findByUserIdOrderByCreatedAtDesc(userId);
    }


    // TEST 8: Successfully retrieve order details

    @Test
    void shouldSuccessfullyRetrieveOrderDetails() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        when(orderItemRepository.findAll())
                .thenReturn(new ArrayList<>());

        var response = orderService.getOrderDetails(userId, orderId);

        assertNotNull(response);

        verify(orderRepository).findById(orderId);
    }


    // TEST 9: Reject access to another customer's order

    @Test
    void shouldThrowExceptionWhenOrderBelongsToAnotherUser() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.getOrderDetails(userId, orderId)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(orderItemRepository, never()).findAll();
    }


    // TEST 10: Successfully cancel a pending order

    @Test
    void shouldSuccessfullyCancelPendingOrder() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);
        Product product = mock(Product.class);
        OrderItem orderItem = mock(OrderItem.class);

        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(order.getStatus()).thenReturn("PENDING");

        when(orderItemRepository.findByOrderId(orderId))
                .thenReturn(List.of(orderItem));

        when(orderItem.getProduct()).thenReturn(product);
        when(orderItem.getQuantity()).thenReturn(2);
        when(product.getStock()).thenReturn(5);

        Order response = orderService.cancelOrder(userId, orderId);

        assertNotNull(response);

        verify(product).setStock(7);
        verify(order).setStatus("CANCELLED");

        verify(orderStatusHistoryRepository)
                .save(any(OrderStatusHistory.class));
    }


    // TEST 11: Reject cancellation of non-pending order

    @Test
    void shouldThrowExceptionWhenCancellingNonPendingOrder() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(order.getStatus()).thenReturn("CONFIRMED");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelOrder(userId, orderId)
        );

        assertEquals(
                "Only pending orders can be cancelled",
                exception.getMessage()
        );

        verify(orderItemRepository, never()).findByOrderId(orderId);

        verify(orderStatusHistoryRepository, never())
                .save(any(OrderStatusHistory.class));
    }


    // TEST 12: Reject cancellation of another customer's order

    @Test
    void shouldThrowExceptionWhenCancellingAnotherUsersOrder() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.cancelOrder(userId, orderId)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(orderItemRepository, never()).findByOrderId(orderId);
    }


    // TEST 13: Successfully retrieve all orders for admin

    @Test
    void shouldSuccessfullyRetrieveAllOrdersForAdmin() {

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Rahul");
        when(user.getEmail()).thenReturn("rahul@example.com");

        List<?> response = orderService.getAllOrdersForAdmin();

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(orderRepository)
                .findAllByOrderByCreatedAtDesc();
    }


    // TEST 14: Successfully update order status

    @Test
    void shouldSuccessfullyUpdateOrderStatus() {

        Long orderId = 10L;

        Order order = mock(Order.class);

        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.of(order));

        when(order.getStatus()).thenReturn("CONFIRMED");

        Order response = orderService.updateOrderStatus(
                orderId,
                "PROCESSING"
        );

        assertNotNull(response);

        verify(order).setStatus("PROCESSING");

        verify(orderStatusHistoryRepository)
                .save(any(OrderStatusHistory.class));
    }


    // TEST 15: Reject invalid order status transition

    @Test
    void shouldThrowExceptionWhenOrderStatusTransitionIsInvalid() {

        Long orderId = 10L;

        Order order = mock(Order.class);

        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.of(order));

        when(order.getStatus()).thenReturn("PENDING");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        "DELIVERED"
                )
        );

        assertEquals(
                "Invalid order status transition from PENDING to DELIVERED",
                exception.getMessage()
        );

        verify(orderStatusHistoryRepository, never())
                .save(any(OrderStatusHistory.class));
    }


    // TEST 16: Successfully retrieve order tracking

    @Test
    void shouldSuccessfullyRetrieveOrderTracking() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);
        OrderStatusHistory history = mock(OrderStatusHistory.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        when(orderStatusHistoryRepository
                .findByOrderIdOrderByChangedAtAsc(orderId))
                .thenReturn(List.of(history));

        List<?> response = orderService.getOrderTracking(userId, orderId);

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(orderStatusHistoryRepository)
                .findByOrderIdOrderByChangedAtAsc(orderId);
    }


    // TEST 17: Reject tracking access to another customer's order

    @Test
    void shouldThrowExceptionWhenTrackingAnotherUsersOrder() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.getOrderTracking(userId, orderId)
        );

        verify(orderStatusHistoryRepository, never())
                .findByOrderIdOrderByChangedAtAsc(orderId);
    }

}
