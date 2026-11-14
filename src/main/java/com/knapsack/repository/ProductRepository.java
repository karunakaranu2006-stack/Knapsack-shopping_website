package com.knapsack.repository;

import com.knapsack.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsFeaturedTrue();

    List<Product> findByCategory_CategoryId(Integer categoryId);

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR (CASE WHEN p.discountPrice IS NOT NULL THEN p.discountPrice ELSE p.price END) >= :minPrice) AND " +
           "(:maxPrice IS NULL OR (CASE WHEN p.discountPrice IS NOT NULL THEN p.discountPrice ELSE p.price END) <= :maxPrice)")
    List<Product> filterProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice);
}
