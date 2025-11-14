package com.dukkan.order.model;

/**
 * Order status enum
 */
public enum OrderStatus {
    PENDING,      // Order placed, awaiting payment
    CONFIRMED,    // Payment confirmed
    PROCESSING,   // Being prepared
    SHIPPED,      // Shipped to customer
    DELIVERED,    // Delivered successfully
    CANCELLED     // Cancelled by user or system
}
