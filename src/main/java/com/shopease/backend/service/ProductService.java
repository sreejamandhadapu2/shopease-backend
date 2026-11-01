
package com.shopease.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopease.backend.dto.CreateProductRequest;
import com.shopease.backend.entity.Category;
import com.shopease.backend.entity.Product;
import com.shopease.backend.exception.CategoryNotFoundException;
import com.shopease.backend.exception.ProductNotFoundException;
import com.shopease.backend.repository.CategoryRepository;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.specification.ProductSpecification;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create product with an existing category
    public Product createProduct(CreateProductRequest request) {

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: "
                                        + request.getCategoryId()));

        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock()
        );

        product.setCategory(category);

        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));
    }

    // Update product
    public Product updateProduct(Long id, Product product) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());

        if (product.getCategory() != null
                && product.getCategory().getId() != null) {

            Category category = categoryRepository
                    .findById(product.getCategory().getId())
                    .orElseThrow(() ->
                            new CategoryNotFoundException(
                                    "Category not found with id: "
                                            + product.getCategory().getId()));

            existingProduct.setCategory(category);
        }

        return productRepository.save(existingProduct);
    }

    // Delete product
    public void deleteProduct(Long id) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        productRepository.delete(existingProduct);
    }

    // Search, filter, sort and paginate products
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(
            String keyword,
            String categoryName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            int page,
            int size,
            String sortBy,
            String direction) {

        // Validate page number
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative");
        }

        // Validate page size
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100");
        }

        // Validate minimum price
        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum price cannot be negative");
        }

        // Validate maximum price
        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum price cannot be negative");
        }

        // Validate price range
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum price cannot exceed maximum price");
        }

        // Allow only valid sorting fields
        List<String> allowedSortFields =
                List.of("id", "name", "price", "stock");

        if (!allowedSortFields.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sorting field. Allowed fields: id, name, price, stock");
        }

        // Validate sorting direction
        if (!"asc".equalsIgnoreCase(direction)
                && !"desc".equalsIgnoreCase(direction)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direction must be asc or desc");
        }

        // Configure sorting
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Configure pagination
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build dynamic search filters
        Specification<Product> specification =
                ProductSpecification.filterProducts(
                        keyword,
                        categoryName,
                        minPrice,
                        maxPrice,
                        inStock
                );

        // Execute filtered and paginated query
        return productRepository.findAll(specification, pageable);
    }
}
