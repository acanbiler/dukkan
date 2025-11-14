package com.dukkan.order.service;

import com.dukkan.order.client.ProductClient;
import com.dukkan.order.client.ProductDTO;
import com.dukkan.order.dto.OrderDTO;
import com.dukkan.order.dto.PlaceOrderRequest;
import com.dukkan.order.mapper.OrderMapper;
import com.dukkan.order.model.Order;
import com.dukkan.order.model.OrderItem;
import com.dukkan.order.model.OrderStatus;
import com.dukkan.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    /**
     * Place a new order
     */
    @Transactional
    public OrderDTO placeOrder(UUID userId, PlaceOrderRequest request) {
        log.info("Placing order for user: {}", userId);

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .build();

        // Process each item
        request.getItems().forEach(itemRequest -> {
            // Fetch product details
            ProductDTO product = productClient.getProduct(itemRequest.getProductId());

            // Validate product is active and in stock
            if (!product.getIsActive()) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            order.addItem(orderItem);

            // Reduce stock
            productClient.reduceStock(product.getId(), itemRequest.getQuantity());
        });

        // Calculate total
        order.calculateTotal();

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully: {}", savedOrder.getOrderNumber());

        return orderMapper.toDTO(savedOrder);
    }

    /**
     * Get user's order history
     */
    public Page<OrderDTO> getUserOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toDTO);
    }

    /**
     * Get order by ID
     */
    public OrderDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toDTO(order);
    }

    /**
     * Cancel order
     */
    @Transactional
    public OrderDTO cancelOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        order.cancel();
        Order savedOrder = orderRepository.save(order);

        log.info("Order cancelled: {}", savedOrder.getOrderNumber());
        return orderMapper.toDTO(savedOrder);
    }
}
