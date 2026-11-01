package com.shopease.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistResponse {

    private Long wishlistItemId;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime addedAt;

    public WishlistResponse(
            Long wishlistItemId,
            Long productId,
            String productName,
            String description,
            BigDecimal price,
            Integer stock,
            LocalDateTime addedAt) {

        this.wishlistItemId = wishlistItemId;
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.addedAt = addedAt;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}