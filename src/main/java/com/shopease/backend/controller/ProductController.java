
package com.shopease.backend.controller;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopease.backend.dto.CreateProductRequest;
import com.shopease.backend.entity.Product;
import com.shopease.backend.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Create product
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product savedProduct = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedProduct);
    }

    // Get all products

@GetMapping
public ResponseEntity<Page<Product>> searchProducts(

        @RequestParam(required = false)
        String keyword,

        @RequestParam(required = false)
        String categoryName,

        @RequestParam(required = false)
        BigDecimal minPrice,

        @RequestParam(required = false)
        BigDecimal maxPrice,

        @RequestParam(required = false)
        Boolean inStock,

        @RequestParam(defaultValue = "0")
        int page,

        @RequestParam(defaultValue = "10")
        int size,

        @RequestParam(defaultValue = "id")
        String sortBy,

        @RequestParam(defaultValue = "asc")
        String direction) {

    Page<Product> products = productService.searchProducts(
            keyword,
            categoryName,
            minPrice,
            maxPrice,
            inStock,
            page,
            size,
            sortBy,
            direction
    );

    return ResponseEntity.ok(products);
}


    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getProductById(id));
    }

    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {

        Product updatedProduct =
                productService.updateProduct(id, product);

        return ResponseEntity.ok(updatedProduct);
    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok("Product deleted successfully");
    }
}
