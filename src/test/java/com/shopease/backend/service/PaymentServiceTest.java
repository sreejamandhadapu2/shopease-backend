
package com.shopease.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopease.backend.entity.Order;
import com.shopease.backend.entity.OrderStatusHistory;
import com.shopease.backend.entity.Payment;
import com.shopease.backend.entity.PaymentStatus;
import com.shopease.backend.entity.User;

import com.shopease.backend.repository.OrderRepository;
import com.shopease.backend.repository.OrderStatusHistoryRepository;
import com.shopease.backend.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private PaymentService paymentService;


    // TEST 1: Successfully create payment

    @Test
    void shouldSuccessfullyCreatePayment() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(order.getStatus()).thenReturn("PENDING");

        when(paymentRepository
                .findTopByOrderIdOrderByCreatedAtDesc(orderId))
                .thenReturn(Optional.empty());

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("2000.00"));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment response = paymentService.createPayment(userId, orderId);

        assertNotNull(response);
        assertEquals(PaymentStatus.CREATED, response.getStatus());
        assertEquals(new BigDecimal("2000.00"), response.getAmount());

        verify(paymentRepository).save(any(Payment.class));
    }


    // TEST 2: Reject payment for another customer's order

    @Test
    void shouldThrowExceptionWhenCreatingPaymentForAnotherUsersOrder() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(userId, orderId)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }


    // TEST 3: Reject payment for non-pending order

    @Test
    void shouldThrowExceptionWhenOrderIsNotPending() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(order.getStatus()).thenReturn("CONFIRMED");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.createPayment(userId, orderId)
        );

        assertEquals(
                "Order is not eligible for payment",
                exception.getMessage()
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }


    // TEST 4: Reject new attempt when previous payment is CREATED

    @Test
    void shouldRejectNewAttemptWhenPreviousPaymentIsNotFailed() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);
        Payment previousPayment = mock(Payment.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(order.getStatus()).thenReturn("PENDING");

        when(paymentRepository
                .findTopByOrderIdOrderByCreatedAtDesc(orderId))
                .thenReturn(Optional.of(previousPayment));

        when(previousPayment.getStatus())
                .thenReturn(PaymentStatus.CREATED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.createPayment(userId, orderId)
        );

        assertTrue(exception.getMessage().contains(
                "A new payment attempt is not allowed"));

        verify(paymentRepository, never()).save(any(Payment.class));
    }


    // TEST 5: Successfully retry after failed payment

    @Test
    void shouldAllowRetryAfterFailedPayment() {

        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);
        Payment previousPayment = mock(Payment.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(order.getStatus()).thenReturn("PENDING");

        when(paymentRepository
                .findTopByOrderIdOrderByCreatedAtDesc(orderId))
                .thenReturn(Optional.of(previousPayment));

        when(previousPayment.getStatus())
                .thenReturn(PaymentStatus.FAILED);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment response = paymentService.createPayment(userId, orderId);

        assertNotNull(response);
        assertEquals(PaymentStatus.CREATED, response.getStatus());

        verify(paymentRepository).save(any(Payment.class));
    }


    // TEST 6: Successfully retrieve payment

    @Test
    void shouldSuccessfullyRetrievePayment() {

        Long userId = 1L;
        Long paymentId = 100L;

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);
        User user = mock(User.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getOrder()).thenReturn(order);
        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        Payment response = paymentService.getPayment(userId, paymentId);

        assertNotNull(response);
        assertEquals(payment, response);
    }


    // TEST 7: Reject payment retrieval by another customer

    @Test
    void shouldThrowExceptionWhenRetrievingAnotherUsersPayment() {

        Long userId = 1L;
        Long paymentId = 100L;

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);
        User user = mock(User.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getOrder()).thenReturn(order);
        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.getPayment(userId, paymentId)
        );

        assertEquals("Payment not found", exception.getMessage());
    }


    // TEST 8: Reject nonexistent payment retrieval

    @Test
    void shouldThrowExceptionWhenPaymentDoesNotExist() {

        Long userId = 1L;
        Long paymentId = 999L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.getPayment(userId, paymentId)
        );

        assertEquals("Payment not found", exception.getMessage());
    }


    // TEST 9: Successfully process payment

    @Test
    void shouldSuccessfullyMarkPaymentAsSuccessful() {

        Long paymentId = 100L;

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.CREATED);

        when(payment.getOrder()).thenReturn(order);
        when(order.getStatus()).thenReturn("PENDING");

        paymentService.markPaymentSuccessful(paymentId);

        verify(payment).setStatus(PaymentStatus.SUCCESS);
        verify(order).setStatus("CONFIRMED");

        verify(orderStatusHistoryRepository)
                .save(any(OrderStatusHistory.class));
    }


    // TEST 10: Prevent duplicate successful payment processing

    @Test
    void shouldNotProcessPaymentTwice() {

        Long paymentId = 100L;

        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.SUCCESS);

        paymentService.markPaymentSuccessful(paymentId);

        verify(payment, never()).setStatus(any(PaymentStatus.class));

        verifyNoInteractions(orderStatusHistoryRepository);
    }


    // TEST 11: Reject failed payment becoming successful

    @Test
    void shouldThrowExceptionWhenFailedPaymentIsMarkedSuccessful() {

        Long paymentId = 100L;

        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.FAILED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.markPaymentSuccessful(paymentId)
        );

        assertEquals(
                "Failed payment cannot be marked successful",
                exception.getMessage()
        );

        verifyNoInteractions(orderStatusHistoryRepository);
    }


    // TEST 12: Reject payment when order is not pending

    @Test
    void shouldThrowExceptionWhenOrderIsNotPendingDuringPayment() {

        Long paymentId = 100L;

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.CREATED);

        when(payment.getOrder()).thenReturn(order);
        when(order.getStatus()).thenReturn("CANCELLED");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.markPaymentSuccessful(paymentId)
        );

        assertEquals(
                "Payment cannot be completed because the order is not pending",
                exception.getMessage()
        );

        verify(payment, never()).setStatus(PaymentStatus.SUCCESS);

        verifyNoInteractions(orderStatusHistoryRepository);
    }


    // TEST 13: Successfully mark payment as failed

    @Test
    void shouldSuccessfullyMarkPaymentAsFailed() {

        Long paymentId = 100L;

        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.CREATED);

        paymentService.markPaymentFailed(paymentId);

        verify(payment).setStatus(PaymentStatus.FAILED);
    }


    // TEST 14: Prevent successful payment from being downgraded

    @Test
    void shouldNotMarkSuccessfulPaymentAsFailed() {

        Long paymentId = 100L;

        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.SUCCESS);

        paymentService.markPaymentFailed(paymentId);

        verify(payment, never()).setStatus(PaymentStatus.FAILED);
    }


    // TEST 15: Reject processing nonexistent payment

    @Test
    void shouldThrowExceptionWhenProcessingNonExistingPayment() {

        Long paymentId = 999L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.markPaymentFailed(paymentId)
        );

        assertEquals("Payment not found", exception.getMessage());
    }


    // TEST 16: Successfully retrieve payment history

    @Test
    void shouldSuccessfullyRetrievePaymentHistory() {

        Long userId = 1L;

        Payment payment1 = mock(Payment.class);
        Payment payment2 = mock(Payment.class);

        when(paymentRepository
                .findByOrderUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(payment1, payment2));

        List<Payment> response = paymentService.getPaymentHistory(userId);

        assertNotNull(response);
        assertEquals(2, response.size());

        verify(paymentRepository)
                .findByOrderUserIdOrderByCreatedAtDesc(userId);
    }

}
