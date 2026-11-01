
package com.shopease.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.shopease.backend.dto.AddToCartRequest;
import com.shopease.backend.dto.CartResponse;
import com.shopease.backend.entity.User;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(
            CartService cartService,
            UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return user.getId();
    }

    @PostMapping("/items")
    public ResponseEntity<String> addItem(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        Long userId = getAuthenticatedUserId(authentication);

        cartService.addItem(
                userId,
                request.getProductId(),
                request.getQuantity());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Product added to cart successfully");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(cartService.getCart(userId));
    }
    @PutMapping("/items/{productId}")
    public ResponseEntity<String> updateItem(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody AddToCartRequest request) {

        Long userId = getAuthenticatedUserId(authentication);

        cartService.updateItem(
                userId,
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok(
                "Cart item quantity updated successfully"
        );
    }
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeItem(
            Authentication authentication,
            @PathVariable Long productId) {

        Long userId = getAuthenticatedUserId(authentication);

        cartService.removeItem(userId, productId);

        return ResponseEntity.ok(
                "Cart item removed successfully"
        );
    }
    @DeleteMapping
    public ResponseEntity<String> clearCart(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}
