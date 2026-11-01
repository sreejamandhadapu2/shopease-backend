
package com.shopease.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopease.backend.dto.CartItemResponse;
import com.shopease.backend.dto.CartResponse;
import com.shopease.backend.entity.Cart;
import com.shopease.backend.entity.CartItem;
import com.shopease.backend.entity.Product;
import com.shopease.backend.entity.User;
import com.shopease.backend.repository.CartItemRepository;
import com.shopease.backend.repository.CartRepository;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new RuntimeException("User not found"));

                    Cart cart = new Cart();
                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public void addItem(Long userId, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(newItem);
        }
    }

@Transactional
public CartResponse getCart(Long userId) {

    Cart cart = getOrCreateCart(userId);

    List<CartItemResponse> items = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (CartItem item : cart.getItems()) {
        BigDecimal price = item.getProduct().getPrice();

        BigDecimal subtotal = price.multiply(
                BigDecimal.valueOf(item.getQuantity()));

        items.add(new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                price,
                item.getQuantity(),
                subtotal
        ));

        total = total.add(subtotal);
    }

    return new CartResponse(cart.getId(), items, total);
}
@Transactional
public void updateItem(Long userId, Long productId, Integer quantity) {

    if (quantity == null || quantity <= 0) {
        throw new IllegalArgumentException(
                "Quantity must be greater than zero"
        );
    }

    Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() ->
                    new RuntimeException("Cart not found"));

    CartItem cartItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Product not found in cart"));

    cartItem.setQuantity(quantity);

    cartItemRepository.save(cartItem);
}
@Transactional
public void removeItem(Long userId, Long productId) {

    Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() ->
                    new RuntimeException("Cart not found"));

    CartItem cartItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Product not found in cart"));

    cartItemRepository.delete(cartItem);
}
@Transactional
public void clearCart(Long userId) {

    Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() ->
                    new RuntimeException("Cart not found"));

    cart.getItems().clear();

    cartRepository.save(cart);
}

}
