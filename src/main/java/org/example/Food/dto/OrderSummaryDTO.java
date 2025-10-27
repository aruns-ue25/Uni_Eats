package org.example.dto;

// Member: Piranavan - Order management
// Related database tables: orders and order item tables

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryDTO {
    private Long id;
    private String orderNumber;
    private String shopName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private Integer itemCount;

    public OrderSummaryDTO() {}

    public OrderSummaryDTO(Long id, String orderNumber, String shopName, BigDecimal totalAmount, String status, String paymentStatus, LocalDateTime createdAt, Integer itemCount) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.shopName = shopName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
}


