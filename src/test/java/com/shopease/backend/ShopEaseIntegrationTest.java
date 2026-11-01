
package com.shopease.backend;

import com.shopease.backend.dto.OrderTrackingResponse;
import com.shopease.backend.entity.Address;
import com.shopease.backend.entity.Cart;
import com.shopease.backend.entity.CartItem;
import com.shopease.backend.entity.Category;
import com.shopease.backend.entity.Order;
import com.shopease.backend.entity.OrderItem;
import com.shopease.backend.entity.OrderStatusHistory;
import com.shopease.backend.entity.Payment;
import com.shopease.backend.entity.PaymentStatus;
import com.shopease.backend.entity.Product;
import com.shopease.backend.entity.User;

import com.shopease.backend.repository.AddressRepository;
import com.shopease.backend.repository.OrderRepository;
import com.shopease.backend.repository.PaymentRepository;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.service.OrderService;
import com.shopease.backend.service.PaymentService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShopEaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private ProductRepository productRepository;
 
    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    


    // Test 1: Save and retrieve a user.
    @Test
    void shouldSaveAndRetrieveUser() {

        User user = new User(
                "Integration User",
                "integration@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);
        entityManager.flush();

        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());

        Long userId = user.getId();

        entityManager.clear();

        User savedUser = entityManager.find(User.class, userId);

        assertNotNull(savedUser);
        assertEquals("Integration User", savedUser.getName());
        assertEquals("integration@example.com", savedUser.getEmail());
        assertEquals("CUSTOMER", savedUser.getRole());
    }


    // Test 2: Save product with its category.
    @Test
    void shouldSaveProductWithCategory() {

        Category category = new Category(
                "Integration Electronics",
                "Electronics for integration testing"
        );

        entityManager.persist(category);

        Product product = new Product(
                "Integration Laptop",
                "Laptop created during integration testing",
                new BigDecimal("55000.00"),
                20
        );

        product.setCategory(category);

        entityManager.persist(product);
        entityManager.flush();

        assertNotNull(product.getId());

        Long productId = product.getId();

        entityManager.clear();

        Product savedProduct =
                entityManager.find(Product.class, productId);

        assertNotNull(savedProduct);
        assertEquals("Integration Laptop", savedProduct.getName());

        assertEquals(
                new BigDecimal("55000.00"),
                savedProduct.getPrice()
        );

        assertEquals(20, savedProduct.getStock());
        assertNotNull(savedProduct.getCategory());

        assertEquals(
                "Integration Electronics",
                savedProduct.getCategory().getName()
        );
    }


    // Test 3: Save address associated with a user.
    @Test
    void shouldSaveAddressForUser() {

        User user = new User(
                "Address Test User",
                "address-test@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);

        Address address = new Address();
        address.setUser(user);
        address.setFullName("Address Test User");
        address.setPhoneNumber("9876543210");
        address.setAddressLine("12 Test Street");
        address.setCity("Hyderabad");
        address.setState("Telangana");
        address.setPincode("500001");
        address.setCountry("India");

        entityManager.persist(address);
        entityManager.flush();

        assertNotNull(address.getId());
        assertNotNull(address.getCreatedAt());

        Address savedAddress = addressRepository
                .findByIdAndUserId(address.getId(), user.getId())
                .orElseThrow();

        assertEquals("Hyderabad", savedAddress.getCity());
        assertEquals("India", savedAddress.getCountry());
        assertEquals(user.getId(), savedAddress.getUser().getId());
    }


    // Test 4: Save cart associated with a user.
    @Test
    void shouldSaveCartForUser() {

        User user = new User(
                "Cart Test User",
                "cart-test@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);

        Cart cart = new Cart();
        cart.setUser(user);

        entityManager.persist(cart);
        entityManager.flush();

        assertNotNull(cart.getId());
        assertNotNull(cart.getCreatedAt());

        Long cartId = cart.getId();
        Long userId = user.getId();

        entityManager.clear();

        Cart savedCart = entityManager.find(Cart.class, cartId);

        assertNotNull(savedCart);
        assertEquals(userId, savedCart.getUser().getId());
    }


    // Test 5: Save cart items through cascade persistence.
    @Test
    void shouldSaveCartItemsThroughCascade() {

        User user = new User(
                "Cart Item User",
                "cart-item@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);

        Product product = new Product(
                "Integration Mouse",
                "Wireless mouse for testing",
                new BigDecimal("799.00"),
                50
        );

        entityManager.persist(product);

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem item = new CartItem(cart, product, 2);
        cart.addItem(item);

        entityManager.persist(cart);
        entityManager.flush();

        assertNotNull(cart.getId());
        assertNotNull(item.getId());

        Long cartId = cart.getId();

        entityManager.clear();

        Cart savedCart = entityManager.find(Cart.class, cartId);

        assertNotNull(savedCart);
        assertEquals(1, savedCart.getItems().size());

        CartItem savedItem = savedCart.getItems().get(0);

        assertEquals(2, savedItem.getQuantity());
        assertEquals("Integration Mouse", savedItem.getProduct().getName());
    }


    // Test 6: Verify orphan removal.
    @Test
    void shouldRemoveCartItemThroughOrphanRemoval() {

        User user = new User(
                "Orphan Test User",
                "orphan-test@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);

        Product product = new Product(
                "Integration Keyboard",
                "Keyboard for orphan removal testing",
                new BigDecimal("1200.00"),
                30
        );

        entityManager.persist(product);

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem item = new CartItem(cart, product, 1);
        cart.addItem(item);

        entityManager.persist(cart);
        entityManager.flush();

        Long itemId = item.getId();

        cart.removeItem(item);

        entityManager.flush();
        entityManager.clear();

        CartItem deletedItem =
                entityManager.find(CartItem.class, itemId);

        assertNull(deletedItem);
    }


    // ==========================================
    // GROUP 2: DATABASE CONSTRAINTS
    // ==========================================

    // Test 7: Reject duplicate user email.
    @Test
    void shouldRejectDuplicateUserEmail() {

        User firstUser = new User(
                "First User",
                "duplicate@example.com",
                "password123"
        );

        User secondUser = new User(
                "Second User",
                "duplicate@example.com",
                "password456"
        );

        entityManager.persist(firstUser);
        entityManager.flush();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    entityManager.persist(secondUser);
                    entityManager.flush();
                }
        );

        assertTrue(isUniqueConstraintViolation(exception));
    }


    // Test 8: Reject multiple carts for the same user.
    @Test
    void shouldRejectMultipleCartsForSameUser() {

        User user = new User(
                "Duplicate Cart User",
                "duplicate-cart@example.com",
                "password123"
        );

        entityManager.persist(user);

        Cart firstCart = new Cart();
        firstCart.setUser(user);

        entityManager.persist(firstCart);
        entityManager.flush();

        Cart secondCart = new Cart();
        secondCart.setUser(user);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    entityManager.persist(secondCart);
                    entityManager.flush();
                }
        );

        assertTrue(isUniqueConstraintViolation(exception));
    }


    // Test 9: Reject duplicate product in the same cart.
    @Test
    void shouldRejectDuplicateProductInSameCart() {

        User user = new User(
                "Duplicate Item User",
                "duplicate-item@example.com",
                "password123"
        );

        entityManager.persist(user);

        Product product = new Product(
                "Duplicate Test Product",
                "Product for testing unique cart items",
                new BigDecimal("500.00"),
                10
        );

        entityManager.persist(product);

        Cart cart = new Cart();
        cart.setUser(user);

        entityManager.persist(cart);

        CartItem firstItem = new CartItem(cart, product, 1);
        entityManager.persist(firstItem);
        entityManager.flush();

        CartItem secondItem = new CartItem(cart, product, 2);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    entityManager.persist(secondItem);
                    entityManager.flush();
                }
        );

        assertTrue(isUniqueConstraintViolation(exception));
    }


    // Test 10: Verify optimistic locking version increment.
    @Test
    void shouldIncrementProductVersionWhenUpdated() {

        Product product = new Product(
                "Version Test Product",
                "Product for optimistic locking test",
                new BigDecimal("1000.00"),
                15
        );

        entityManager.persist(product);
        entityManager.flush();

        Object initialVersion = entityManager
                .getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getVersion(product);

        product.setStock(10);

        entityManager.flush();

        Object updatedVersion = entityManager
                .getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getVersion(product);

        assertNotNull(initialVersion);
        assertNotNull(updatedVersion);

        assertEquals(
                ((Number) initialVersion).longValue() + 1,
                ((Number) updatedVersion).longValue()
        );
    }


    // ==========================================
    // GROUP 3: CHECKOUT AND STOCK MANAGEMENT
    // ==========================================

    // Test 11: Successfully create an order.
    @Test
    void shouldSuccessfullyCreateOrder() {

        CheckoutFixture fixture = prepareCheckout(20, 2);

        Order order = orderService.createOrder(
                fixture.user.getId(),
                fixture.address.getId()
        );

        assertNotNull(order.getId());
        assertEquals("PENDING", order.getStatus());

        assertEquals(
                new BigDecimal("2000.00"),
                order.getTotalAmount()
        );

        assertEquals(18, fixture.product.getStock());
        assertTrue(fixture.cart.getItems().isEmpty());

        assertEquals("Hyderabad", order.getDeliveryCity());
    }


    // Test 12: Reject checkout with an empty cart.
    @Test
    void shouldRejectCheckoutWithEmptyCart() {

        CheckoutFixture fixture = prepareEmptyCheckout();

        assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrder(
                        fixture.user.getId(),
                        fixture.address.getId()
                )
        );
    }


    // Test 13: Reject checkout when stock is insufficient.
    @Test
    void shouldRejectCheckoutWhenStockIsInsufficient() {

        CheckoutFixture fixture = prepareCheckout(1, 3);

        assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrder(
                        fixture.user.getId(),
                        fixture.address.getId()
                )
        );

        assertEquals(1, fixture.product.getStock());
    }


    // ==========================================
    // HELPER: DATABASE CONSTRAINT EXCEPTION
    // ==========================================

    private boolean isUniqueConstraintViolation(Throwable exception) {

        Throwable current = exception;

        while (current != null) {

            if (current instanceof
                    org.hibernate.exception.ConstraintViolationException) {
                return true;
            }

            if (current instanceof SQLException) {

                if ("23505".equals(
                        ((SQLException) current).getSQLState())) {
                    return true;
                }
            }

            current = current.getCause();
        }

        return false;
    }


    // ==========================================
    // HELPER: CHECKOUT TEST DATA
    // ==========================================

    private CheckoutFixture prepareCheckout(
            int stock,
            int quantity) {

        User user = new User(
                "Checkout Customer",
                "checkout-" + System.nanoTime() + "@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);

        Address address = createCheckoutAddress(user);

        Product product = new Product(
                "Checkout Test Product",
                "Product for checkout testing",
                new BigDecimal("1000.00"),
                stock
        );

        entityManager.persist(product);

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem item = new CartItem(cart, product, quantity);
        cart.addItem(item);

        entityManager.persist(cart);
        entityManager.flush();

        return new CheckoutFixture(
                user,
                address,
                product,
                cart
        );
    }


    private CheckoutFixture prepareEmptyCheckout() {

        User user = new User(
                "Empty Cart Customer",
                "empty-" + System.nanoTime() + "@example.com",
                "encodedPassword123"
        );

        entityManager.persist(user);

        Address address = createCheckoutAddress(user);

        Cart cart = new Cart();
        cart.setUser(user);

        entityManager.persist(cart);
        entityManager.flush();

        return new CheckoutFixture(
                user,
                address,
                null,
                cart
        );
    }


    private Address createCheckoutAddress(User user) {

        Address address = new Address();

        address.setUser(user);
        address.setFullName("Checkout Customer");
        address.setPhoneNumber("9876543210");
        address.setAddressLine("12 Test Street");
        address.setCity("Hyderabad");
        address.setState("Telangana");
        address.setPincode("500001");
        address.setCountry("India");

        entityManager.persist(address);

        return address;
    }


    // ==========================================
    // CHECKOUT FIXTURE
    // ==========================================

    private static class CheckoutFixture {

        private final User user;
        private final Address address;
        private final Product product;
        private final Cart cart;

        CheckoutFixture(
                User user,
                Address address,
                Product product,
                Cart cart) {

            this.user = user;
            this.address = address;
            this.product = product;
            this.cart = cart;
        }
    }

@Test
void shouldSaveOrderItemWithPurchaseTimePrice() {

    CheckoutFixture fixture = prepareCheckout(20, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    entityManager.flush();

    List<OrderItem> items = entityManager.createQuery(
            "SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId",
            OrderItem.class
    )
    .setParameter("orderId", order.getId())
    .getResultList();

    assertEquals(1, items.size());

    assertEquals(
            new BigDecimal("1000.00"),
            items.get(0).getPrice()
    );

    assertEquals(2, items.get(0).getQuantity());
}

@Test
void shouldPreservePurchasePriceAfterProductPriceChanges() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    fixture.product.setPrice(new BigDecimal("1500.00"));

    entityManager.flush();

    List<OrderItem> items = entityManager.createQuery(
            "SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId",
            OrderItem.class
    )
    .setParameter("orderId", order.getId())
    .getResultList();

    assertEquals(1, items.size());

    assertEquals(
            new BigDecimal("1000.00"),
            items.get(0).getPrice()
    );

    assertEquals(
            new BigDecimal("1500.00"),
            fixture.product.getPrice()
    );
}

@Test
void shouldPreserveDeliveryAddressSnapshotAfterAddressChanges() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    fixture.address.setCity("Vijayawada");
    fixture.address.setAddressLine("Updated Address");

    entityManager.flush();

    assertEquals("Hyderabad", order.getDeliveryCity());

    assertEquals(
            "12 Test Street",
            order.getDeliveryAddressLine()
    );

    assertEquals(
            "Checkout Customer",
            order.getDeliveryFullName()
    );
}

@Test
void shouldCreatePaymentForPendingOrder() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    assertNotNull(payment.getId());

    assertEquals(
            PaymentStatus.CREATED,
            payment.getStatus()
    );

    assertEquals(
            order.getTotalAmount(),
            payment.getAmount()
    );

    assertEquals("PENDING", order.getStatus());
}


@Test
void shouldConfirmOrderAfterSuccessfulPayment() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    paymentService.markPaymentSuccessful(payment.getId());

    assertEquals(
            PaymentStatus.SUCCESS,
            payment.getStatus()
    );

    assertEquals("CONFIRMED", order.getStatus());

    List<OrderStatusHistory> history =
            entityManager.createQuery(
                    "SELECT h FROM OrderStatusHistory h WHERE h.order.id = :orderId",
                    OrderStatusHistory.class
            )
            .setParameter("orderId", order.getId())
            .getResultList();

    assertEquals(1, history.size());

    assertEquals(
            "PENDING",
            history.get(0).getPreviousStatus()
    );

    assertEquals(
            "CONFIRMED",
            history.get(0).getNewStatus()
    );
}


@Test
void shouldRejectNewPaymentWhenPreviousAttemptIsNotFailed() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment firstPayment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    assertThrows(
            IllegalStateException.class,
            () -> paymentService.createPayment(
                    fixture.user.getId(),
                    order.getId()
            )
    );

    assertEquals(
            PaymentStatus.CREATED,
            firstPayment.getStatus()
    );
}


@Test
void shouldCreatePaymentForPendingOrder1() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    assertNotNull(payment.getId());

    assertEquals(
            PaymentStatus.CREATED,
            payment.getStatus()
    );

    assertEquals(
            order.getTotalAmount(),
            payment.getAmount()
    );

    assertEquals("PENDING", order.getStatus());
}


@Test
void shouldConfirmOrderAfterSuccessfulPayment1() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    paymentService.markPaymentSuccessful(payment.getId());

    assertEquals(
            PaymentStatus.SUCCESS,
            payment.getStatus()
    );

    assertEquals("CONFIRMED", order.getStatus());

    List<OrderStatusHistory> history =
            entityManager.createQuery(
                    "SELECT h FROM OrderStatusHistory h WHERE h.order.id = :orderId",
                    OrderStatusHistory.class
            )
            .setParameter("orderId", order.getId())
            .getResultList();

    assertEquals(1, history.size());

    assertEquals(
            "PENDING",
            history.get(0).getPreviousStatus()
    );

    assertEquals(
            "CONFIRMED",
            history.get(0).getNewStatus()
    );
}


@Test
void shouldRejectNewPaymentWhenPreviousAttemptIsNotFailed1() {

    CheckoutFixture fixture = prepareCheckout(20, 1);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment firstPayment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    assertThrows(
            IllegalStateException.class,
            () -> paymentService.createPayment(
                    fixture.user.getId(),
                    order.getId()
            )
    );

    assertEquals(
            PaymentStatus.CREATED,
            firstPayment.getStatus()
    );
}
@Test
void shouldRestoreProductStockWhenOrderIsCancelled() {
    CheckoutFixture fixture = prepareCheckout(10, 3);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Product productAfterCheckout = productRepository
            .findById(fixture.product.getId())
            .orElseThrow();

    assertThat(productAfterCheckout.getStock()).isEqualTo(7);

    orderService.cancelOrder(fixture.user.getId(), order.getId());

    Product productAfterCancellation = productRepository
            .findById(fixture.product.getId())
            .orElseThrow();

    assertThat(productAfterCancellation.getStock()).isEqualTo(10);
}
@Test
void shouldRejectCancellingAnAlreadyCancelledOrder() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    orderService.cancelOrder(fixture.user.getId(), order.getId());

    assertThatThrownBy(() ->
            orderService.cancelOrder(fixture.user.getId(), order.getId())
    ).isInstanceOf(IllegalStateException.class);
}	
@Test
void shouldAllowConfirmedOrderToMoveToProcessing() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    Payment payment = paymentRepository
            .findTopByOrderIdOrderByCreatedAtDesc(order.getId())
            .orElseThrow();

    paymentService.markPaymentSuccessful(payment.getId());

    Order updatedOrder = orderService.updateOrderStatus(
            order.getId(),
            "PROCESSING"
    );

    assertThat(updatedOrder.getStatus())
    .isEqualTo("PROCESSING");
}
@Test
void shouldRejectSkippingOrderStatusToDelivered() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    Payment payment = paymentRepository
            .findTopByOrderIdOrderByCreatedAtDesc(order.getId())
            .orElseThrow();

    paymentService.markPaymentSuccessful(payment.getId());

    assertThatThrownBy(() ->
            orderService.updateOrderStatus(
                    order.getId(),
                    "DELIVERED"
            )
    ).isInstanceOf(IllegalStateException.class);
}
@Test
void shouldRejectCancellingAnotherUsersOrder() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    User anotherUser = new User();
    anotherUser.setName("Another Customer");
    anotherUser.setEmail("another@example.com");
    anotherUser.setPassword("password123");
    anotherUser.setRole("CUSTOMER");

    anotherUser = userRepository.save(anotherUser);

    Long anotherUserId = anotherUser.getId();

    assertThatThrownBy(() ->
            orderService.cancelOrder(anotherUserId, order.getId())
    ).isInstanceOf(RuntimeException.class);
}
@Test
void shouldRejectViewingAnotherUsersOrder() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    User anotherUser = new User();
    anotherUser.setName("Another Customer");
    anotherUser.setEmail("another2@example.com");
    anotherUser.setPassword("password123");
    anotherUser.setRole("CUSTOMER");

    anotherUser = userRepository.save(anotherUser);

    Long anotherUserId = anotherUser.getId();

    assertThatThrownBy(() ->
            orderService.getOrderDetails(anotherUserId, order.getId())
    ).isInstanceOf(RuntimeException.class);
}
@Test
void shouldRetrieveOwnPaymentHistory() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    List<Payment> payments =
            paymentService.getPaymentHistory(fixture.user.getId());

    assertThat(payments)
            .extracting(Payment::getId)
            .contains(payment.getId());
}
@Test
void shouldRejectAccessingAnotherUsersPayment() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    User anotherUser = new User();
    anotherUser.setName("Another Customer");
    anotherUser.setEmail("paymentuser@example.com");
    anotherUser.setPassword("password123");
    anotherUser.setRole("CUSTOMER");

    anotherUser = userRepository.save(anotherUser);

    Long anotherUserId = anotherUser.getId();

    assertThatThrownBy(() ->
            paymentService.getPayment(anotherUserId, payment.getId())
    ).isInstanceOf(RuntimeException.class);
}
@Test
void shouldReturnOnlyOwnPaymentsInPaymentHistory() {
    CheckoutFixture firstFixture = prepareCheckout(10, 2);

    Order firstOrder = orderService.createOrder(
            firstFixture.user.getId(),
            firstFixture.address.getId()
    );

    Payment firstPayment = paymentService.createPayment(
            firstFixture.user.getId(),
            firstOrder.getId()
    );

    CheckoutFixture secondFixture = prepareCheckout(10, 2);

    Order secondOrder = orderService.createOrder(
            secondFixture.user.getId(),
            secondFixture.address.getId()
    );

    Payment secondPayment = paymentService.createPayment(
            secondFixture.user.getId(),
            secondOrder.getId()
    );

    List<Payment> firstUserPayments =
            paymentService.getPaymentHistory(firstFixture.user.getId());

    assertThat(firstUserPayments)
            .extracting(Payment::getId)
            .contains(firstPayment.getId())
            .doesNotContain(secondPayment.getId());
}
@Test
void shouldRetrieveOwnOrderTrackingHistory() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    Payment payment = paymentService.createPayment(
            fixture.user.getId(),
            order.getId()
    );

    paymentService.markPaymentSuccessful(payment.getId());

    List<OrderTrackingResponse> tracking =
            orderService.getOrderTracking(
                    fixture.user.getId(),
                    order.getId()
            );

    assertThat(tracking).isNotEmpty();
}
@Test
void shouldRejectAccessingAnotherUsersOrderTracking() {
    CheckoutFixture fixture = prepareCheckout(10, 2);

    Order order = orderService.createOrder(
            fixture.user.getId(),
            fixture.address.getId()
    );

    User anotherUser = new User();
    anotherUser.setName("Tracking Customer");
    anotherUser.setEmail("trackinguser@example.com");
    anotherUser.setPassword("password123");
    anotherUser.setRole("CUSTOMER");

    anotherUser = userRepository.save(anotherUser);

    Long anotherUserId = anotherUser.getId();

    assertThatThrownBy(() ->
            orderService.getOrderTracking(
                    anotherUserId,
                    order.getId()
            )
    ).isInstanceOf(RuntimeException.class);
}
}
