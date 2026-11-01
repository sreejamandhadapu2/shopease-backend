
package com.shopease.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shopease.backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);


}
