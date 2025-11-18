package com.dukkan.order.repository;

import com.dukkan.order.model.Order;
import com.dukkan.order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OrderRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByOrderNumber_WhenOrderExists_ShouldReturnOrder() {
        // Given
        Order order = Order.builder()
                .userId(UUID.randomUUID())
                .orderNumber("TEST-ORDER-001")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        orderRepository.save(order);

        // When
        Optional<Order> found = orderRepository.findByOrderNumber("TEST-ORDER-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("TEST-ORDER-001");
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void findByOrderNumber_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Order> found = orderRepository.findByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnPagedResults() {
        // Given
        UUID userId = UUID.randomUUID();

        Order order1 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-PAGED-001")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        Order order2 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-PAGED-002")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        Order otherUserOrder = Order.builder()
                .userId(UUID.randomUUID()) // Different user
                .orderNumber("TEST-PAGED-003")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(50.00))
                .build();

        orderRepository.saveAll(List.of(order1, order2, otherUserOrder));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(userId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("userId")
                .containsOnly(userId);
    }

    @Test
    void findByUserIdAndStatus_ShouldFilterByStatus() {
        // Given
        UUID userId = UUID.randomUUID();

        Order pendingOrder1 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-STATUS-001")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        Order pendingOrder2 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-STATUS-002")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(150.00))
                .build();

        Order confirmedOrder = Order.builder()
                .userId(userId)
                .orderNumber("TEST-STATUS-003")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        orderRepository.saveAll(List.of(pendingOrder1, pendingOrder2, confirmedOrder));

        // When
        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING);

        // Then
        assertThat(pendingOrders).hasSize(2);
        assertThat(pendingOrders).extracting("status")
                .containsOnly(OrderStatus.PENDING);
    }

    @Test
    void findRecentOrdersByUserId_ShouldReturnOrderedByCreatedAtDesc() throws InterruptedException {
        // Given
        UUID userId = UUID.randomUUID();

        Order order1 = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        orderRepository.save(order1);

        Thread.sleep(10); // Small delay to ensure different timestamps

        Order order2 = Order.builder()
                .userId(userId)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();
        orderRepository.save(order2);

        Thread.sleep(10);

        Order order3 = Order.builder()
                .userId(userId)
                .status(OrderStatus.SHIPPED)
                .totalAmount(BigDecimal.valueOf(300.00))
                .build();
        orderRepository.save(order3);

        Pageable pageable = PageRequest.of(0, 2);

        // When
        List<Order> recentOrders = orderRepository.findRecentOrdersByUserId(userId, pageable);

        // Then
        assertThat(recentOrders).hasSize(2);
        // Most recent first (order3 and order2)
        assertThat(recentOrders.get(0).getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(recentOrders.get(1).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void countByUserId_ShouldReturnCorrectCount() {
        // Given
        UUID userId = UUID.randomUUID();

        Order order1 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-COUNT-001")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        Order order2 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-COUNT-002")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        Order order3 = Order.builder()
                .userId(userId)
                .orderNumber("TEST-COUNT-003")
                .status(OrderStatus.CANCELLED)
                .totalAmount(BigDecimal.valueOf(300.00))
                .build();

        Order otherUserOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderNumber("TEST-COUNT-004")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(50.00))
                .build();

        orderRepository.saveAll(List.of(order1, order2, order3, otherUserOrder));

        // When
        long count = orderRepository.countByUserId(userId);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void save_ShouldGenerateIdAndOrderNumber() {
        // Given
        Order order = Order.builder()
                .userId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(150.00))
                .build();

        // When
        Order saved = orderRepository.save(order);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderNumber()).isNotNull();
        assertThat(saved.getOrderNumber()).startsWith("ORD");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_ShouldEnforceOrderNumberUniqueness() {
        // Given
        String orderNumber = "UNIQUE-ORDER-123";

        Order order1 = Order.builder()
                .userId(UUID.randomUUID())
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .userId(UUID.randomUUID())
                .orderNumber(orderNumber) // Duplicate
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        // When / Then
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> {
                    orderRepository.save(order2);
                    orderRepository.flush();
                }
        );
    }
}
