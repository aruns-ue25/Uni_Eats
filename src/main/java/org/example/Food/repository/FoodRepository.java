package org.example.repository;

import org.example.model.Food;
import org.example.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    List<Food> findByShop(Shop shop);
    
    List<Food> findByShopAndIsAvailable(Shop shop, Boolean isAvailable);
    
    List<Food> findByCategory(String category);
    
    List<Food> findByIsAvailable(Boolean isAvailable);
    
    @Query("SELECT f FROM Food f WHERE f.shop = :shop AND f.isAvailable = true ORDER BY f.name")
    List<Food> findAvailableByShop(@Param("shop") Shop shop);
    
    @Query("SELECT f FROM Food f WHERE f.category = :category AND f.isAvailable = true ORDER BY f.rating DESC")
    List<Food> findAvailableByCategoryOrderByRating(@Param("category") String category);
    
    @Query("SELECT f FROM Food f WHERE f.name LIKE %:name% AND f.isAvailable = true")
    List<Food> findByNameContainingAndAvailable(@Param("name") String name);
    
    @Query("SELECT f FROM Food f WHERE f.price BETWEEN :minPrice AND :maxPrice AND f.isAvailable = true")
    List<Food> findByPriceRangeAndAvailable(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT f FROM Food f WHERE f.shop.isApproved = true AND f.shop.isActive = true AND f.isAvailable = true")
    List<Food> findAllAvailableFromApprovedShops();
    
    List<Food> findByIsFeaturedAndIsAvailable(Boolean isFeatured, Boolean isAvailable);
    
    List<Food> findByShopAndIsFeaturedAndIsAvailable(Shop shop, Boolean isFeatured, Boolean isAvailable);
    
    List<Food> findByDiscountPercentageGreaterThanAndIsAvailable(BigDecimal discountPercentage, Boolean isAvailable);
    
    List<Food> findByCaloriesBetweenAndIsAvailable(Integer minCalories, Integer maxCalories, Boolean isAvailable);
    
    @Query("SELECT f FROM Food f WHERE f.ingredients LIKE %:ingredient% AND f.isAvailable = :isAvailable")
    List<Food> findByIngredientsContainingAndIsAvailable(@Param("ingredient") String ingredient, @Param("isAvailable") Boolean isAvailable);
    
    @Query("SELECT f FROM Food f WHERE f.allergens LIKE %:allergen% AND f.isAvailable = :isAvailable")
    List<Food> findByAllergensContainingAndIsAvailable(@Param("allergen") String allergen, @Param("isAvailable") Boolean isAvailable);
    
    @Query("SELECT f FROM Food f WHERE f.shop.isApproved = true AND f.shop.isActive = true AND f.isAvailable = true ORDER BY f.rating DESC")
    List<Food> findAllAvailableFromApprovedShopsOrderByRating();
    
    @Query("SELECT f FROM Food f WHERE f.shop.isApproved = true AND f.shop.isActive = true AND f.isAvailable = true ORDER BY f.totalOrders DESC")
    List<Food> findAllAvailableFromApprovedShopsOrderByPopularity();
    
    @Query("SELECT f FROM Food f WHERE f.shop.isApproved = true AND f.shop.isActive = true AND f.isAvailable = true ORDER BY f.price ASC")
    List<Food> findAllAvailableFromApprovedShopsOrderByPriceAsc();
    
    @Query("SELECT f FROM Food f WHERE f.shop.isApproved = true AND f.shop.isActive = true AND f.isAvailable = true ORDER BY f.price DESC")
    List<Food> findAllAvailableFromApprovedShopsOrderByPriceDesc();
}
