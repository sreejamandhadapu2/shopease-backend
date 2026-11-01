
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

import com.shopease.backend.entity.Cart;
import com.shopease.backend.entity.CartItem;
import com.shopease.backend.entity.Product;
import com.shopease.backend.entity.User;
import com.shopease.backend.repository.CartItemRepository;
import com.shopease.backend.repository.CartRepository;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;


    // TEST 1: Successfully add a new product to cart

    @Test
    void shouldSuccessfullyAddProductToCart() {

        Long userId = 1L;
        Long productId = 10L;

        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.empty());

        cartService.addItem(userId, productId, 2);

        verify(cartItemRepository, times(1))
                .save(any(CartItem.class));
    }


    // TEST 2: Reject zero or negative quantity

    @Test
    void shouldThrowExceptionWhenQuantityIsInvalid() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(1L, 10L, 0)
        );

        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository,
                productRepository,
                userRepository
        );
    }


    // TEST 3: Reject product that does not exist

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        Long userId = 1L;
        Long productId = 999L;

        Cart cart = mock(Cart.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addItem(userId, productId, 2)
        );

        assertEquals("Product not found", exception.getMessage());

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }


    // TEST 4: Increase quantity when product already exists

    @Test
    void shouldIncreaseQuantityWhenProductAlreadyExistsInCart() {

        Long userId = 1L;
        Long productId = 10L;

        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);
        CartItem existingItem = mock(CartItem.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.of(existingItem));

        when(existingItem.getQuantity()).thenReturn(2);

        cartService.addItem(userId, productId, 3);

        verify(existingItem).setQuantity(5);

        verify(cartItemRepository).save(existingItem);
    }


    // TEST 5: Create cart when customer does not have one

    @Test
    void shouldCreateCartWhenCustomerDoesNotHaveOne() {

        Long userId = 1L;
        Long productId = 10L;

        User user = mock(User.class);
        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cart);

        when(cart.getId()).thenReturn(1L);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.empty());

        cartService.addItem(userId, productId, 1);

        verify(cartRepository).save(any(Cart.class));

        verify(cartItemRepository).save(any(CartItem.class));
    }


    // TEST 6: Reject cart creation when user does not exist

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        Long userId = 999L;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addItem(userId, 10L, 1)
        );

        assertEquals("User not found", exception.getMessage());

        verify(cartRepository, never()).save(any(Cart.class));
    }


    // TEST 7: Successfully retrieve cart

    @Test
    void shouldSuccessfullyRetrieveCart() {

        Long userId = 1L;

        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);
        when(cart.getItems()).thenReturn(List.of(item));

        when(item.getProduct()).thenReturn(product);
        when(item.getQuantity()).thenReturn(2);

        when(product.getId()).thenReturn(10L);
        when(product.getName()).thenReturn("Bluetooth Speaker");
        when(product.getPrice()).thenReturn(new BigDecimal("2299.00"));

        var response = cartService.getCart(userId);

        assertNotNull(response);

        verify(cartRepository).findByUserId(userId);
    }


    // TEST 8: Retrieve empty cart

    @Test
    void shouldSuccessfullyRetrieveEmptyCart() {

        Long userId = 1L;

        Cart cart = mock(Cart.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);
        when(cart.getItems()).thenReturn(new ArrayList<>());

        var response = cartService.getCart(userId);

        assertNotNull(response);

        verify(cartRepository).findByUserId(userId);
    }


    // TEST 9: Reject update when quantity is invalid

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidQuantity() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateItem(1L, 10L, 0)
        );

        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(cartRepository, cartItemRepository);
    }


    // TEST 10: Successfully update cart item quantity

    @Test
    void shouldSuccessfullyUpdateCartItemQuantity() {

        Long userId = 1L;
        Long productId = 10L;

        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.of(item));

        cartService.updateItem(userId, productId, 5);

        verify(item).setQuantity(5);

        verify(cartItemRepository).save(item);
    }


    // TEST 11: Reject update when cart does not exist

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingCart() {

        Long userId = 1L;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.updateItem(userId, 10L, 3)
        );

        assertEquals("Cart not found", exception.getMessage());
    }


    // TEST 12: Reject update when product is not in cart

    @Test
    void shouldThrowExceptionWhenUpdatingProductNotInCart() {

        Long userId = 1L;
        Long productId = 10L;

        Cart cart = mock(Cart.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.updateItem(userId, productId, 3)
        );

        assertEquals("Product not found in cart", exception.getMessage());

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }


    // TEST 13: Successfully remove cart item

    @Test
    void shouldSuccessfullyRemoveCartItem() {

        Long userId = 1L;
        Long productId = 10L;

        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.of(item));

        cartService.removeItem(userId, productId);

        verify(cartItemRepository).delete(item);
    }


    // TEST 14: Reject removal when cart does not exist

    @Test
    void shouldThrowExceptionWhenRemovingFromNonExistingCart() {

        Long userId = 1L;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(userId, 10L)
        );

        assertEquals("Cart not found", exception.getMessage());

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }


    // TEST 15: Reject removal when product is not in cart

    @Test
    void shouldThrowExceptionWhenRemovingProductNotInCart() {

        Long userId = 1L;
        Long productId = 10L;

        Cart cart = mock(Cart.class);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(1L);

        when(cartItemRepository.findByCartIdAndProductId(1L, productId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(userId, productId)
        );

        assertEquals("Product not found in cart", exception.getMessage());

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }


    // TEST 16: Successfully clear cart

    @Test
    void shouldSuccessfullyClearCart() {

        Long userId = 1L;

        Cart cart = mock(Cart.class);

        List<CartItem> items = new ArrayList<>();

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cart.getItems()).thenReturn(items);

        cartService.clearCart(userId);

        assertTrue(items.isEmpty());

        verify(cartRepository).save(cart);
    }


    // TEST 17: Reject clearing when cart does not exist

    @Test
    void shouldThrowExceptionWhenClearingNonExistingCart() {

        Long userId = 1L;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.clearCart(userId)
        );

        assertEquals("Cart not found", exception.getMessage());

        verify(cartRepository, never()).save(any(Cart.class));
    }

}
