package com.shopease.backend.controller;

import com.shopease.backend.dto.WishlistResponse;
import com.shopease.backend.service.WishlistService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistResponse> addToWishlist(
            Authentication authentication,
            @RequestParam Long productId) {

        String email = authentication.getName();

        WishlistResponse response =
                wishlistService.addToWishlist(email, productId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist(
            Authentication authentication) {

        String email = authentication.getName();

        List<WishlistResponse> response =
                wishlistService.getWishlist(email);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {

        String email = authentication.getName();

        wishlistService.removeFromWishlist(email, productId);

        return ResponseEntity.noContent().build();
    }
}