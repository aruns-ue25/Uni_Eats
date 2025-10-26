package org.example.controller;

// Member: Heshan - User Registration and Customer management (customer profile management)
// Member: Piranavan - Order management (checkout and order viewing endpoints)
// Related database tables: user & customer tables, orders and order item tables

import org.example.model.*;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private FoodService foodService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    // Get current customer info
    @GetMapping("/profile")
    public ResponseEntity<Customer> getCustomerProfile(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(customer);
    }
    
    // Update customer profile
    @PutMapping("/profile")
    public ResponseEntity<Customer> updateCustomerProfile(@RequestBody Customer customer, Authentication authentication) {
        String email = authentication.getName();
        Customer currentCustomer = customerService.findByEmail(email).orElseThrow();
        customer.setId(currentCustomer.getId());
        Customer updatedCustomer = customerService.updateCustomer(customer);
        return ResponseEntity.ok(updatedCustomer);
    }
    
    // Delete customer account
    @DeleteMapping("/profile")
    public ResponseEntity<Map<String, String>> deleteCustomerProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            Customer currentCustomer = customerService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            customerService.deleteCustomer(currentCustomer.getId());
            
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting customer account: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete account: " + e.getMessage()));
        }
    }
    
    // Dashboard statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        
        long totalOrders = orderService.countOrdersByCustomer(customer);
        List<Order> recentOrders = orderService.getOrdersByCustomer(customer);
        
        return ResponseEntity.ok(Map.of(
            "totalOrders", totalOrders,
            "totalSpent", customer.getTotalSpent(),
            "recentOrdersCount", recentOrders.size()
        ));
    }
    
    // Browse shops
    @GetMapping("/shops")
    public ResponseEntity<List<Shop>> getApprovedShops() {
        return ResponseEntity.ok(shopService.getApprovedShops());
    }
    
    @GetMapping("/shops/search")
    public ResponseEntity<List<Shop>> searchShops(@RequestParam String name) {
        return ResponseEntity.ok(shopService.searchShopsByName(name));
    }
    
    @GetMapping("/shops/city/{city}")
    public ResponseEntity<List<Shop>> getShopsByCity(@PathVariable String city) {
        return ResponseEntity.ok(shopService.getShopsByCity(city));
    }
    
    // Browse foods
    @GetMapping("/foods")
    public ResponseEntity<List<Food>> getAllAvailableFoods() {
        return ResponseEntity.ok(foodService.getAllAvailableFoods());
    }
    
    @GetMapping("/foods/shop/{shopId}")
    public ResponseEntity<List<Food>> getFoodsByShop(@PathVariable Long shopId) {
        Shop shop = shopService.findById(shopId);
        return ResponseEntity.ok(foodService.getAvailableFoodsByShop(shop));
    }
    
    @GetMapping("/foods/category/{category}")
    public ResponseEntity<List<Food>> getFoodsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(foodService.getFoodsByCategory(category));
    }
    
    @GetMapping("/foods/search")
    public ResponseEntity<List<Food>> searchFoods(@RequestParam String name) {
        return ResponseEntity.ok(foodService.searchFoodsByName(name));
    }
    
    @GetMapping("/foods/price-range")
    public ResponseEntity<List<Food>> getFoodsByPriceRange(@RequestParam Double minPrice, @RequestParam Double maxPrice) {
        return ResponseEntity.ok(foodService.getFoodsByPriceRange(minPrice, maxPrice));
    }
    
    @GetMapping("/foods/{foodId}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long foodId) {
        Food food = foodService.findById(foodId);
        return ResponseEntity.ok(food);
    }
    
    // Order management
    @GetMapping("/orders")
    public ResponseEntity<List<org.example.dto.OrderSummaryDTO>> getCustomerOrders(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        List<Order> orders = orderService.getOrdersByCustomer(customer);
        List<org.example.dto.OrderSummaryDTO> dto = orders.stream().map(o -> new org.example.dto.OrderSummaryDTO(
                o.getId(),
                o.getOrderNumber(),
                o.getShop() != null ? o.getShop().getShopName() : "",
                o.getTotalAmount(),
                o.getStatus().name(),
                o.getPaymentStatus().name(),
                o.getCreatedAt(),
                o.getOrderItems() != null ? o.getOrderItems().size() : 0
        )).toList();
        return ResponseEntity.ok(dto);
    }

    // Recent orders (summary)
    @GetMapping("/orders/recent")
    public ResponseEntity<List<org.example.dto.OrderSummaryDTO>> getRecentCustomerOrders(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        List<Order> orders = orderService.getOrdersByCustomer(customer);
        List<org.example.dto.OrderSummaryDTO> dto = orders.stream()
                .limit(10)
                .map(o -> new org.example.dto.OrderSummaryDTO(
                        o.getId(),
                        o.getOrderNumber(),
                        o.getShop() != null ? o.getShop().getShopName() : "",
                        o.getTotalAmount(),
                        o.getStatus().name(),
                        o.getPaymentStatus().name(),
                        o.getCreatedAt(),
                        o.getOrderItems() != null ? o.getOrderItems().size() : 0
                )).toList();
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        Order order = orderService.findById(orderId);
        
        if (!order.getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(order);
    }

    // Order details with items and shop name for modal rendering
    @GetMapping("/orders/{orderId}/details")
    public ResponseEntity<org.example.dto.OrderWithItemsDTO> getOrderDetails(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        Order order = orderService.findById(orderId);
        if (!order.getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.badRequest().build();
        }
        java.util.List<org.example.dto.OrderItemDTO> items = order.getOrderItems().stream().map(oi ->
                new org.example.dto.OrderItemDTO(
                        oi.getFood().getId(),
                        oi.getFood().getName(),
                        oi.getQuantity(),
                        oi.getUnitPrice(),
                        oi.getTotalPrice()
                )
        ).toList();
        org.example.dto.OrderWithItemsDTO dto = new org.example.dto.OrderWithItemsDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getCreatedAt(),
                items
        );
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        Order order = orderService.findById(orderId);
        
        if (!order.getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        Order cancelledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelledOrder);
    }
    
    // Activity logs
    @GetMapping("/activity-logs")
    public ResponseEntity<List<ActivityLog>> getActivityLogs(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(activityLogService.getActivityLogsByUser(customer));
    }

    // Checkout: create an order from cart items
    @PostMapping("/orders/checkout")
    public ResponseEntity<Order> checkout(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.findByEmail(email).orElseThrow();

        Long shopId = Long.valueOf(String.valueOf(payload.get("shopId")));
        Shop shop = shopService.findById(shopId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        // Calculate total and create order
        final java.math.BigDecimal[] totalHolder = new java.math.BigDecimal[]{java.math.BigDecimal.ZERO};
        Order order = new Order(customer, shop, java.math.BigDecimal.ZERO);

        List<OrderItem> orderItems = items.stream().map(it -> {
            Long foodId = Long.valueOf(String.valueOf(it.get("foodId")));
            Integer quantity = Integer.valueOf(String.valueOf(it.get("quantity")));
            Food food = foodService.findById(foodId);
            if (!food.getShop().getId().equals(shop.getId())) {
                throw new RuntimeException("All items must belong to the same shop");
            }
            java.math.BigDecimal unitPrice = food.getPrice();
            java.math.BigDecimal lineTotal = unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
            totalHolder[0] = totalHolder[0].add(lineTotal);

            OrderItem oi = new OrderItem(quantity, unitPrice, order, food);
            return oi;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalHolder[0]);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PAID);

        Order saved = orderService.createOrder(order);

        // Persist items
        saved.getOrderItems().forEach(oi -> oi.setOrder(saved));
        // Note: OrderItem persistence is cascaded by Order mapping

        // Update counters
        orderItems.forEach(oi -> {
            foodService.incrementFoodOrders(oi.getFood().getId());
        });

        return ResponseEntity.ok(saved);
    }
}
