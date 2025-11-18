package com.dukkan.order.service;

import com.dukkan.order.client.ProductClient;
import com.dukkan.order.client.ProductDTO;
import com.dukkan.order.dto.OrderDTO;
import com.dukkan.order.dto.OrderItemRequest;
import com.dukkan.order.dto.PlaceOrderRequest;
import com.dukkan.order.mapper.OrderMapper;
import com.dukkan.order.model.Order;
import com.dukkan.order.model.OrderStatus;
import com.dukkan.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_WithValidRequest_ShouldSucceed() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(Arrays.asList(itemRequest))
                .build();

        ProductDTO product = ProductDTO.builder()
                .id(productId)
                .name("Test Product")
                .sku("TEST-SKU")
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .isActive(true)
                .build();

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderNumber("ORD123456")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .build();

        when(productClient.getProduct(productId)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDTO(savedOrder)).thenReturn(orderDTO);

        // When
        OrderDTO result = orderService.placeOrder(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD123456");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(productClient).getProduct(productId);
        verify(productClient).reduceStock(productId, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_WithInactiveProduct_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(Arrays.asList(itemRequest))
                .build();

        ProductDTO inactiveProduct = ProductDTO.builder()
                .id(productId)
                .name("Inactive Product")
                .sku("INACTIVE-SKU")
                .price(new BigDecimal("50.00"))
                .stockQuantity(10)
                .isActive(false) // Product not active
                .build();

        when(productClient.getProduct(productId)).thenReturn(inactiveProduct);

        // When / Then
        assertThatThrownBy(() -> orderService.placeOrder(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product is not available");

        verify(productClient).getProduct(productId);
        verify(productClient, never()).reduceStock(any(), anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_WithInsufficientStock_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(10) // Requesting 10
                .build();

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(Arrays.asList(itemRequest))
                .build();

        ProductDTO product = ProductDTO.builder()
                .id(productId)
                .name("Low Stock Product")
                .sku("LOW-STOCK")
                .price(new BigDecimal("75.00"))
                .stockQuantity(5) // Only 5 available
                .isActive(true)
                .build();

        when(productClient.getProduct(productId)).thenReturn(product);

        // When / Then
        assertThatThrownBy(() -> orderService.placeOrder(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");

        verify(productClient).getProduct(productId);
        verify(productClient, never()).reduceStock(any(), anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldReduceStockForEachItem() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        OrderItemRequest item1 = OrderItemRequest.builder()
                .productId(productId1)
                .quantity(3)
                .build();

        OrderItemRequest item2 = OrderItemRequest.builder()
                .productId(productId2)
                .quantity(5)
                .build();

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(Arrays.asList(item1, item2))
                .build();

        ProductDTO product1 = ProductDTO.builder()
                .id(productId1)
                .name("Product 1")
                .sku("SKU-1")
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .isActive(true)
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .id(productId2)
                .name("Product 2")
                .sku("SKU-2")
                .price(new BigDecimal("200.00"))
                .stockQuantity(20)
                .isActive(true)
                .build();

        when(productClient.getProduct(productId1)).thenReturn(product1);
        when(productClient.getProduct(productId2)).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());
        when(orderMapper.toDTO(any())).thenReturn(new OrderDTO());

        // When
        orderService.placeOrder(userId, request);

        // Then
        verify(productClient).reduceStock(productId1, 3);
        verify(productClient).reduceStock(productId2, 5);
    }

    @Test
    void getUserOrders_ShouldReturnPagedResults() {
        // Given
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Order order1 = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderNumber("ORD001")
                .status(OrderStatus.PENDING)
                .build();

        Order order2 = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderNumber("ORD002")
                .status(OrderStatus.CONFIRMED)
                .build();

        List<Order> orders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        OrderDTO dto1 = OrderDTO.builder().orderNumber("ORD001").build();
        OrderDTO dto2 = OrderDTO.builder().orderNumber("ORD002").build();

        when(orderRepository.findByUserId(userId, pageable)).thenReturn(orderPage);
        when(orderMapper.toDTO(order1)).thenReturn(dto1);
        when(orderMapper.toDTO(order2)).thenReturn(dto2);

        // When
        Page<OrderDTO> result = orderService.getUserOrders(userId, pageable);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getContent()).extracting("orderNumber")
                .containsExactly("ORD001", "ORD002");

        verify(orderRepository).findByUserId(userId, pageable);
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD999")
                .status(OrderStatus.PENDING)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .orderNumber("ORD999")
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        // When
        OrderDTO result = orderService.getOrderById(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD999");
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository).findById(orderId);
    }

    @Test
    void cancelOrder_WithValidRequest_ShouldSucceed() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .orderNumber("ORD123")
                .status(OrderStatus.PENDING)
                .build();

        Order cancelledOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .orderNumber("ORD123")
                .status(OrderStatus.CANCELLED)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .orderNumber("ORD123")
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(cancelledOrder);
        when(orderMapper.toDTO(cancelledOrder)).thenReturn(orderDTO);

        // When
        OrderDTO result = orderService.cancelOrder(userId, orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_WhenOrderNotFound_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> orderService.cancelOrder(userId, orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenUnauthorized_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID(); // Different user
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .userId(differentUserId) // Order belongs to different user
                .orderNumber("ORD123")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When / Then
        assertThatThrownBy(() -> orderService.cancelOrder(userId, orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderAlreadyShipped_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order shippedOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .orderNumber("ORD123")
                .status(OrderStatus.SHIPPED) // Already shipped
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(shippedOrder));

        // When / Then
        assertThatThrownBy(() -> orderService.cancelOrder(userId, orderId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order cannot be cancelled");

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
