package org.example.controller;

// Member: Arun - Shop & Menu management (public API for browsing restaurants and menus)
// Related database tables: shop and food tables

import org.example.model.Food;
import org.example.model.Shop;
import org.example.service.FoodService;
import org.example.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private FoodService foodService;

    // List approved restaurants
    @GetMapping("/restaurants")
    public ResponseEntity<List<Shop>> getRestaurants() {
        return ResponseEntity.ok(shopService.getApprovedShops());
    }

    // Restaurant details
    @GetMapping("/restaurants/{shopId}")
    public ResponseEntity<Shop> getRestaurant(@PathVariable Long shopId) {
        Shop shop = shopService.findById(shopId);
        return ResponseEntity.ok(shop);
    }

    // Restaurant menu
    @GetMapping("/restaurants/{shopId}/menu")
    public ResponseEntity<List<Food>> getRestaurantMenu(@PathVariable Long shopId) {
        Shop shop = shopService.findById(shopId);
        return ResponseEntity.ok(foodService.getAvailableFoodsByShop(shop));
    }
}


