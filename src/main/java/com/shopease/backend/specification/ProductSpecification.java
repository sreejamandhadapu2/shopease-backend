
package com.shopease.backend.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.shopease.backend.entity.Product;

import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

    public static Specification<Product> filterProducts(
            String keyword,
            String categoryName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Search by product name or description
            if (keyword != null && !keyword.isBlank()) {

                String searchKeyword =
                        "%" + keyword.toLowerCase().trim() + "%";

                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            searchKeyword
                        ),
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            searchKeyword
                        )
                    )
                );
            }

            // Filter by category name
            if (categoryName != null && !categoryName.isBlank()) {

                predicates.add(
                    criteriaBuilder.equal(
                        criteriaBuilder.lower(
                            root.get("category").get("name")
                        ),
                        categoryName.trim().toLowerCase()
                    )
                );
            }

            // Filter by minimum price
            if (minPrice != null) {

                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minPrice
                    )
                );
            }

            // Filter by maximum price
            if (maxPrice != null) {

                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maxPrice
                    )
                );
            }

            // Show only products currently in stock

if (inStock != null) {

    if (inStock) {
        predicates.add(
            criteriaBuilder.greaterThan(
                root.get("stock"), 0
            )
        );
    } else {
        predicates.add(
            criteriaBuilder.equal(
                root.get("stock"), 0
            )
        );
    }
}


            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
