package org.example.config;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component  // Re-enabled to ensure initial data is created
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Only initialize if no users exist
            if (userRepository.count() == 0) {
                initializeData();
            }
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeData() {
        try {
            // Create admin user
            Admin admin = new Admin();
            admin.setEmail("admin@foodsystem.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("System Administrator");
            admin.setPhoneNumber("+1234567890");
            admin.setIsActive(true);
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin = (Admin) userRepository.save(admin);

            // Create sample shop
            Shop shop1 = new Shop();
            shop1.setEmail("pizza@example.com");
            shop1.setPassword(passwordEncoder.encode("admin123"));
            shop1.setName("John Pizza");
            shop1.setPhoneNumber("+1111111111");
            shop1.setIsActive(true);
            shop1.setRole(UserRole.SHOP);
            shop1.setCreatedAt(LocalDateTime.now());
            shop1.setUpdatedAt(LocalDateTime.now());
            shop1.setShopName("Pizza Palace");
            shop1.setDescription("Best pizza in town with fresh ingredients");
            shop1.setAddress("123 Main St");
            shop1.setCity("New York");
            shop1.setPostalCode("10001");
            shop1.setIsApproved(true);
            shop1.setRating(new BigDecimal("4.5"));
            shop1.setTotalOrders(150);
            shop1 = (Shop) userRepository.save(shop1);

            // Create sample customer
            Customer customer1 = new Customer();
            customer1.setEmail("customer1@example.com");
            customer1.setPassword(passwordEncoder.encode("admin123"));
            customer1.setName("Alice Johnson");
            customer1.setPhoneNumber("+4444444444");
            customer1.setIsActive(true);
            customer1.setRole(UserRole.CUSTOMER);
            customer1.setCreatedAt(LocalDateTime.now());
            customer1.setUpdatedAt(LocalDateTime.now());
            customer1.setAddress("100 Customer St");
            customer1.setCity("New York");
            customer1.setPostalCode("10004");
            customer1.setDateOfBirth(LocalDate.of(1990, 1, 15));
            customer1.setPreferredPaymentMethod("Credit Card");
            customer1.setTotalOrders(25);
            customer1.setTotalSpent(new BigDecimal("450.00"));
            customer1 = (Customer) userRepository.save(customer1);

            System.out.println("Initial data created successfully!");
        } catch (Exception e) {
            System.err.println("Error creating initial data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
