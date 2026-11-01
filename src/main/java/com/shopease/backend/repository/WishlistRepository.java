package com.shopease.backend.repository;

import com.shopease.backend.entity.WishlistItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository
        extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<WishlistItem> findByUserIdAndProductId(
            Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}