package org.example.service;

// Member: Arun - Shop & Menu Management
// - Shop registration & profile management
// - Menu management: items, categories, pricing, availability, search/filters
// - Shop dashboard and statistics
// Related database tables: shop and food tables

import org.example.model.Food;
import org.example.model.Order;
import org.example.model.Shop;
import org.example.repository.ShopRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShopService {
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    public Shop registerShop(Shop shop) {
        // Check if email already exists in users table (includes all user types)
        if (userRepository.existsByEmail(shop.getEmail())) {
            throw new RuntimeException("A user with this email already exists. Please use a different email address.");
        }
        
        shop.setPassword(passwordEncoder.encode(shop.getPassword()));
        shop.setIsApproved(false); // New shops need admin approval
        Shop savedShop = shopRepository.save(shop);
        
        activityLogService.logActivity("SHOP_REGISTERED", "Shop registered: " + shop.getShopName(), savedShop);
        
        return savedShop;
    }
    
    public Shop approveShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        
        shop.setIsApproved(true);
        Shop approvedShop = shopRepository.save(shop);
        
        activityLogService.logActivity("SHOP_APPROVED", "Shop approved: " + shop.getShopName(), approvedShop);
        
        return approvedShop;
    }
    
    public Shop rejectShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        
        shop.setIsActive(false);
        Shop rejectedShop = shopRepository.save(shop);
        
        activityLogService.logActivity("SHOP_REJECTED", "Shop rejected: " + shop.getShopName(), rejectedShop);
        
        return rejectedShop;
    }
    
    public Shop updateShop(Shop shop) {
        Shop existingShop = shopRepository.findById(shop.getId())
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shop.getId()));
        
        existingShop.setShopName(shop.getShopName());
        existingShop.setDescription(shop.getDescription());
        existingShop.setAddress(shop.getAddress());
        existingShop.setCity(shop.getCity());
        existingShop.setPostalCode(shop.getPostalCode());
        existingShop.setPhoneNumber(shop.getPhoneNumber());
        
        Shop updatedShop = shopRepository.save(existingShop);
        
        activityLogService.logActivity("SHOP_UPDATED", "Shop updated: " + shop.getShopName(), updatedShop);
        
        return updatedShop;
    }
    
    public Optional<Shop> findByEmail(String email) {
        return shopRepository.findByEmail(email);
    }
    
    public Shop findById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));
    }
    
    public List<Shop> getPendingApprovalShops() {
        return shopRepository.findByIsApprovedAndIsActive(false, true);
    }
    
    public List<Shop> getApprovedShops() {
        return shopRepository.findByIsApprovedAndIsActive(true, true);
    }
    
    public List<Shop> getActiveShops() {
        return shopRepository.findByIsActive(true);
    }

    public List<Shop> getInactiveShops() {
        return shopRepository.findByIsActive(false);
    }
    
    public List<Shop> getShopsByCity(String city) {
        return shopRepository.findByCityAndApproved(city);
    }
    
    public List<Shop> searchShopsByName(String name) {
        return shopRepository.findByShopNameContainingAndApproved(name);
    }
    
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }
    
  public void updateShopRating(Long shopId, Double newRating) {
        Shop shop = findById(shopId);
        BigDecimal ratingValue = BigDecimal.valueOf(newRating)
                .setScale(2, RoundingMode.HALF_UP);
        shop.setRating(ratingValue);
        shopRepository.save(shop);
    }
    
    public void incrementShopOrders(Long shopId) {
        Shop shop = findById(shopId);
        shop.setTotalOrders(shop.getTotalOrders() + 1);
        shopRepository.save(shop);
    }
    
    @Transactional
    public void deleteShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        
        // Log the deletion before actually deleting
        activityLogService.logActivity("SHOP_DELETED", "Shop deleted: " + shop.getShopName(), shop);
        
        // First, delete all foods associated with this shop
        List<Food> foods = shop.getFoods();
        if (foods != null && !foods.isEmpty()) {
            for (Food food : new ArrayList<>(foods)) {
                // Remove the food from the shop's list first
                shop.getFoods().remove(food);
                // The food will be deleted due to cascade
            }
        }
        
        // Then, handle orders associated with this shop
        List<Order> orders = shop.getOrders();
        if (orders != null && !orders.isEmpty()) {
            for (Order order : new ArrayList<>(orders)) {
                // Remove the order from the shop's list first
                shop.getOrders().remove(order);
                // The order will be deleted due to cascade
            }
        }
        
        // Delete the shop (this will cascade to related entities based on JPA mappings)
        shopRepository.delete(shop);
    }
}
