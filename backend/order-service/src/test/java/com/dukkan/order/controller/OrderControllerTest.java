package com.dukkan.order.controller;

import com.dukkan.order.dto.OrderDTO;
import com.dukkan.order.dto.OrderItemRequest;
import com.dukkan.order.dto.PlaceOrderRequest;
import com.dukkan.order.model.OrderStatus;
import com.dukkan.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API tests for OrderController
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void placeOrder_WithValidRequest_ShouldReturn201() throws Exception {
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

        OrderDTO response = OrderDTO.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD123456")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .build();

        when(orderService.placeOrder(eq(userId), any(PlaceOrderRequest.class))).thenReturn(response);

        // When / Then
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD123456"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(200.00));
    }

    @Test
    void placeOrder_WithMissingUserId_ShouldReturn500() throws Exception {
        // Given
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(Arrays.asList(itemRequest))
                .build();

        // When / Then - Missing header causes internal error, not bad request
        mockMvc.perform(post("/api/v1/orders")
                        // Missing X-User-Id header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void placeOrder_WithInvalidRequest_ShouldReturn400() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        PlaceOrderRequest emptyRequest = PlaceOrderRequest.builder()
                .items(Arrays.asList()) // Empty items list
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyOrders_ShouldReturn200WithPagedResults() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        OrderDTO order1 = OrderDTO.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD001")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        OrderDTO order2 = OrderDTO.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD002")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("200.00"))
                .build();

        List<OrderDTO> orders = Arrays.asList(order1, order2);
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), orders.size());

        when(orderService.getUserOrders(eq(userId), any())).thenReturn(orderPage);

        // When / Then
        mockMvc.perform(get("/api/v1/orders/my-orders")
                        .header("X-User-Id", userId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD001"))
                .andExpect(jsonPath("$.content[1].orderNumber").value("ORD002"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getMyOrders_WithDefaultPagination_ShouldReturn200() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Page<OrderDTO> emptyPage = new PageImpl<>(Arrays.asList());

        when(orderService.getUserOrders(eq(userId), any())).thenReturn(emptyPage);

        // When / Then
        mockMvc.perform(get("/api/v1/orders/my-orders")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getOrder_WhenOrderExists_ShouldReturn200() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();

        OrderDTO order = OrderDTO.builder()
                .id(orderId)
                .orderNumber("ORD999")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(order);

        // When / Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD999"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(500.00));
    }

    @Test
    void getOrder_WhenOrderNotFound_ShouldReturn404() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();

        when(orderService.getOrderById(orderId))
                .thenThrow(new RuntimeException("Order not found"));

        // When / Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_WithValidRequest_ShouldReturn200() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderDTO cancelledOrder = OrderDTO.builder()
                .id(orderId)
                .orderNumber("ORD123")
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("300.00"))
                .build();

        when(orderService.cancelOrder(userId, orderId)).thenReturn(cancelledOrder);

        // When / Then
        mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_WhenUnauthorized_ShouldReturn403() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderService.cancelOrder(userId, orderId))
                .thenThrow(new RuntimeException("Unauthorized"));

        // When / Then
        mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelOrder_WhenOrderCannotBeCancelled_ShouldReturn400() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderService.cancelOrder(userId, orderId))
                .thenThrow(new IllegalStateException("Order cannot be cancelled"));

        // When / Then
        mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isBadRequest());
    }
}
