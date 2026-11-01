package com.shopease.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopease.backend.entity.User;
import com.shopease.backend.dto.WishlistResponse;
import com.shopease.backend.entity.Product;
import com.shopease.backend.entity.WishlistItem;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.repository.WishlistRepository;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void shouldThrowExceptionWhenProductAlreadyExistsInWishlist() {

        // Arrange
        String email = "sreeja@example.com";
        Long productId = 10L;
        Long userId = 1L;

        User user = mock(User.class);
        Product product = mock(Product.class);
        WishlistItem existingItem = mock(WishlistItem.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(Optional.of(existingItem));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            wishlistService.addToWishlist(email, productId);
        });

        verify(wishlistRepository, never()).saveAndFlush(any(WishlistItem.class));
    }
    @Test
    void shouldSuccessfullyAddProductToWishlist() {

        // Arrange
        String email = "sreeja@example.com";
        Long productId = 10L;
        Long userId = 1L;

        User user = mock(User.class);
        Product product = mock(Product.class);
        WishlistItem savedItem = mock(WishlistItem.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(Optional.empty());

        when(wishlistRepository.saveAndFlush(any(WishlistItem.class)))
                .thenReturn(savedItem);

        when(savedItem.getProduct()).thenReturn(product);

        // Act
        WishlistResponse response =
                wishlistService.addToWishlist(email, productId);

        // Assert
        assertNotNull(response);

        verify(wishlistRepository, times(1))
                .saveAndFlush(any(WishlistItem.class));
    }
    @Test
    void shouldSuccessfullyRetrieveWishlist() {

        // Arrange
        String email = "sreeja@example.com";
        Long userId = 1L;

        User user = mock(User.class);
        WishlistItem item = mock(WishlistItem.class);
        Product product = mock(Product.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);

        when(wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(item));

        when(item.getProduct()).thenReturn(product);
        when(item.getId()).thenReturn(1L);
        when(product.getId()).thenReturn(10L);
        when(product.getName()).thenReturn("Bluetooth Speaker");
        when(product.getDescription()).thenReturn("Portable wireless speaker");
        when(product.getPrice()).thenReturn(new java.math.BigDecimal("2299.00"));
        when(product.getStock()).thenReturn(25);

        // Act
        List<WishlistResponse> response =
                wishlistService.getWishlist(email);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Bluetooth Speaker", response.get(0).getProductName());

        verify(wishlistRepository, times(1))
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        // Arrange
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wishlistService.getWishlist(email)
        );

        assertEquals("User not found", exception.getMessage());

        verify(wishlistRepository, never())
                .findByUserIdOrderByCreatedAtDesc(anyLong());
    }
    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        // Arrange
        String email = "sreeja@example.com";
        Long productId = 999L;

        User user = mock(User.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wishlistService.addToWishlist(email, productId)
        );

        assertEquals("Product not found", exception.getMessage());

        verify(wishlistRepository, never())
                .saveAndFlush(any(WishlistItem.class));
    }
    @Test
    void shouldSuccessfullyRemoveProductFromWishlist() {

        // Arrange
        String email = "sreeja@example.com";
        Long userId = 1L;
        Long productId = 10L;

        User user = mock(User.class);
        WishlistItem item = mock(WishlistItem.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);

        when(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(Optional.of(item));

        // Act
        wishlistService.removeFromWishlist(email, productId);

        // Assert
        verify(wishlistRepository, times(1)).delete(item);
    }
    @Test
    void shouldThrowExceptionWhenRemovingNonExistingWishlistItem() {

        // Arrange
        String email = "sreeja@example.com";
        Long userId = 1L;
        Long productId = 999L;

        User user = mock(User.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);

        when(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wishlistService.removeFromWishlist(email, productId)
        );

        assertEquals("Product not found in wishlist", exception.getMessage());

        verify(wishlistRepository, never()).delete(any(WishlistItem.class));
    }
}