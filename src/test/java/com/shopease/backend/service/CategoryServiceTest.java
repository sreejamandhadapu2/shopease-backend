package com.shopease.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.shopease.backend.entity.Category;
import com.shopease.backend.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;
    
    @Test
    void shouldCreateCategorySuccessfully() {
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic products");

        when(categoryRepository.existsByNameIgnoreCase("Electronics"))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.createCategory(category);

        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription())
                .isEqualTo("Electronic products");

        verify(categoryRepository).save(category);
    }
    @Test
    void shouldRejectDuplicateCategory() {
        Category category = new Category();
        category.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                categoryService.createCategory(category))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Category already exists: Electronics");

        verify(categoryRepository, never()).save(any(Category.class));
    }
    @Test
    void shouldRetrieveAllCategories() {
        Category category1 = new Category();
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setName("Clothing");

        when(categoryRepository.findAll())
                .thenReturn(List.of(category1, category2));

        List<Category> categories = categoryService.getAllCategories();

        assertThat(categories).hasSize(2);
        assertThat(categories)
                .extracting(Category::getName)
                .containsExactly("Electronics", "Clothing");

        verify(categoryRepository).findAll();
    }
    @Test
    void shouldRetrieveCategoryById() {
        Category category = new Category();
        category.setName("Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result.getName()).isEqualTo("Electronics");

        verify(categoryRepository).findById(1L);
    }
    @Test
    void shouldRejectRetrievingNonExistingCategory() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                categoryService.getCategoryById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found with id: 99");
    }
    @Test
    void shouldUpdateCategorySuccessfully() {
        Category existingCategory = new Category();
        existingCategory.setName("Electronics");
        existingCategory.setDescription("Old description");

        Category updatedCategory = new Category();
        updatedCategory.setName("Gadgets");
        updatedCategory.setDescription("Updated description");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(existingCategory));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, updatedCategory);

        assertThat(result.getName()).isEqualTo("Gadgets");
        assertThat(result.getDescription()).isEqualTo("Updated description");

        verify(categoryRepository).save(existingCategory);
    }
    @Test
    void shouldRejectUpdatingNonExistingCategory() {
        Category updatedCategory = new Category();
        updatedCategory.setName("Gadgets");

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                categoryService.updateCategory(99L, updatedCategory))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found with id: 99");

        verify(categoryRepository, never()).save(any(Category.class));
    }
    @Test
    void shouldDeleteCategorySuccessfully() {
        Category category = new Category();
        category.setName("Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }
    @Test
    void shouldRejectDeletingNonExistingCategory() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                categoryService.deleteCategory(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found with id: 99");

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}