
package com.shopease.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal total;

    public CartResponse(
            Long cartId,
            List<CartItemResponse> items,
            BigDecimal total) {
        this.cartId = cartId;
        this.items = items;
        this.total = total;
    }

    public Long getCartId() {
        return cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
