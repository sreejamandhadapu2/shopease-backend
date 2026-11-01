package com.shopease.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import com.shopease.backend.entity.Category;
import com.shopease.backend.entity.Product;
import com.shopease.backend.repository.CategoryRepository;
import com.shopease.backend.repository.ProductRepository;
import com.shopease.backend.dto.CreateProductRequest;
import com.shopease.backend.exception.CategoryNotFoundException;
import com.shopease.backend.exception.ProductNotFoundException;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;
    
    @Test
    void shouldCreateProductSuccessfully() {

        Category category = new Category();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(new BigDecimal("75000"));
        request.setStock(10);
        request.setCategoryId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.createProduct(request);

        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getDescription()).isEqualTo("Gaming Laptop");
        assertThat(product.getPrice()).isEqualByComparingTo("75000");
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(product.getCategory()).isSameAs(category);

        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
    @Test
    void shouldRejectProductCreationWithInvalidCategory() {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(new BigDecimal("75000"));
        request.setStock(10);
        request.setCategoryId(99L);

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.createProduct(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(productRepository, never()).save(any(Product.class));
    }
    @Test
    void shouldRetrieveAllProducts() {
        Product product1 = new Product();
        product1.setName("Laptop");

        Product product2 = new Product();
        product2.setName("Mobile");

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        List<Product> products = productService.getAllProducts();

        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(Product::getName)
                .containsExactly("Laptop", "Mobile");

        verify(productRepository).findAll();
    }
    @Test
    void shouldRetrieveProductById() {
        Product product = new Product();
        product.setName("Laptop");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertThat(result.getName()).isEqualTo("Laptop");

        verify(productRepository).findById(1L);
    }
    @Test
    void shouldUpdateProductSuccessfully() {
        Product existingProduct = new Product();
        existingProduct.setName("Old Laptop");
        existingProduct.setDescription("Old Description");
        existingProduct.setPrice(new BigDecimal("50000"));
        existingProduct.setStock(5);

        Product updatedProduct = new Product();
        updatedProduct.setName("Gaming Laptop");
        updatedProduct.setDescription("High Performance Laptop");
        updatedProduct.setPrice(new BigDecimal("80000"));
        updatedProduct.setStock(15);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updatedProduct);

        assertThat(result.getName()).isEqualTo("Gaming Laptop");
        assertThat(result.getDescription())
                .isEqualTo("High Performance Laptop");
        assertThat(result.getPrice()).isEqualByComparingTo("80000");
        assertThat(result.getStock()).isEqualTo(15);

        verify(productRepository).save(existingProduct);
    }
    @Test
    void shouldRejectUpdatingNonExistingProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Gaming Laptop");

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.updateProduct(99L, updatedProduct))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 99");

        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    void shouldDeleteProductSuccessfully() {
        Product product = new Product();
        product.setName("Laptop");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }
    @Test
    void shouldRejectDeletingNonExistingProduct() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 99");

        verify(productRepository, never()).delete(any(Product.class));
    }
    @Test
    void shouldSearchProductsWithPaginationAndSorting() {
        Product product = new Product();
        product.setName("Laptop");

        Pageable pageable = PageRequest.of(
                1, 5, Sort.by("name").descending()
        );

        Page<Product> expectedPage = new PageImpl<>(
                List.of(product), pageable, 6
        );

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.searchProducts(
                "Laptop",
                null,
                null,
                null,
                true,
                1,
                5,
                "name",
                "desc"
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName())
                .isEqualTo("Laptop");
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(6);

        verify(productRepository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }
    @Test
    void shouldRejectNegativePageNumber() {
        assertThatThrownBy(() ->
                productService.searchProducts(
                        null, null, null, null, null,
                        -1, 10, "id", "asc"
                )
        ).isInstanceOf(ResponseStatusException.class)
         .hasMessageContaining("Page number cannot be negative");

        verify(productRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }
    @Test
    void shouldRejectPageSizeGreaterThan100() {
        assertThatThrownBy(() ->
                productService.searchProducts(
                        null, null, null, null, null,
                        0, 101, "id", "asc"
                ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Page size must be between 1 and 100");
    }
    @Test
    void shouldRejectNegativeMinimumPrice() {
        assertThatThrownBy(() ->
                productService.searchProducts(
                        null, null, new BigDecimal("-100"),
                        null, null, 0, 10, "id", "asc"
                ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Minimum price cannot be negative");
    }
    @Test
    void shouldRejectInvalidPriceRange() {
        assertThatThrownBy(() ->
                productService.searchProducts(
                        null, null, new BigDecimal("1000"),
                        new BigDecimal("500"), null,
                        0, 10, "id", "asc"
                ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Minimum price cannot exceed maximum price");
    }
    @Test
    void shouldRejectInvalidSortingField() {
        assertThatThrownBy(() ->
                productService.searchProducts(
                        null, null, null, null, null,
                        0, 10, "category", "asc"
                ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid sorting field");

        verify(productRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }
    @Test
    void shouldRejectInvalidSortingDirection() {
        assertThatThrownBy(() ->
                productService.searchProducts(
                        null, null, null, null, null,
                        0, 10, "price", "random"
                ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Direction must be asc or desc");

        verify(productRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }
}
