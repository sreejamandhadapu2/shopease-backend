package com.shopease.backend.service;

import com.shopease.backend.dto.WishlistResponse;
import com.shopease.backend.entity.Product;
import com.shopease.backend.entity.User;
import com.shopease.backend.entity.WishlistItem;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.repository.UserRepository;
import com.shopease.backend.repository.WishlistRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public WishlistResponse addToWishlist(String email, Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (wishlistRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .isPresent()) {

            throw new IllegalStateException(
                    "Product already exists in wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);

        WishlistItem savedItem;

        try {
            savedItem = wishlistRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Product already exists in wishlist");
        }

        return toWishlistResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return wishlistRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toWishlistResponse)
                .toList();
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        WishlistItem item = wishlistRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found in wishlist"));

        wishlistRepository.delete(item);
    }

    private WishlistResponse toWishlistResponse(WishlistItem item) {

        Product product = item.getProduct();

        return new WishlistResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                item.getCreatedAt()
        );
    }
}