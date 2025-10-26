package org.example.service;

// Member: Heshan - User Registration and Customer management
// Related database tables: user & customer tables

import org.example.model.Customer;
import org.example.repository.CustomerRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    public Customer registerCustomer(Customer customer) {
        // Check if email already exists in users table (includes all user types)
        if (userRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("A user with this email already exists. Please use a different email address.");
        }
        
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setIsActive(true); // Customers are active immediately
        Customer savedCustomer = customerRepository.save(customer);
        
        activityLogService.logActivity("CUSTOMER_REGISTERED", "Customer registered: " + customer.getName(), savedCustomer);
        
        return savedCustomer;
    }
    
    public Customer updateCustomer(Customer customer) {
        Customer existingCustomer = customerRepository.findById(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customer.getId()));
        
        existingCustomer.setName(customer.getName());
        existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setCity(customer.getCity());
        existingCustomer.setPostalCode(customer.getPostalCode());
        existingCustomer.setDateOfBirth(customer.getDateOfBirth());
        existingCustomer.setPreferredPaymentMethod(customer.getPreferredPaymentMethod());
        
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        
        activityLogService.logActivity("CUSTOMER_UPDATED", "Customer updated: " + customer.getName(), updatedCustomer);
        
        return updatedCustomer;
    }
    
    public void deactivateCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        customer.setIsActive(false);
        customerRepository.save(customer);
        
        activityLogService.logActivity("CUSTOMER_DEACTIVATED", "Customer deactivated: " + customer.getName(), customer);
    }
    
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
    
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }
    
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByIsActive(true);
    }
    
    public List<Customer> getCustomersByCity(String city) {
        return customerRepository.findByCityAndActive(city);
    }
    
    public List<Customer> getTopCustomersBySpending(BigDecimal minAmount) {
        return customerRepository.findTopCustomersBySpending(minAmount);
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public void updateCustomerStats(Long customerId, Double orderAmount) {
        Customer customer = findById(customerId);
        customer.setTotalOrders(customer.getTotalOrders() + 1);
        BigDecimal updatedTotal = customer.getTotalSpent().add(BigDecimal.valueOf(orderAmount));
        customer.setTotalSpent(updatedTotal);
        customerRepository.save(customer);
    }
    
    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        // Log the deletion before actually deleting
        activityLogService.logActivity("CUSTOMER_DELETED", "Customer deleted: " + customer.getName(), customer);
        
        // Delete the customer - JPA cascade will handle deletion of related orders and order items
        // The @CascadeType.ALL on the orders relationship will automatically delete:
        // 1. All orders associated with this customer
        // 2. All order items associated with those orders (due to cascade on Order.orderItems)
        customerRepository.delete(customer);
    }
}
