package com.dukkan.product.controller;

import com.dukkan.product.dto.ProductDTO;
import com.dukkan.product.exception.ResourceNotFoundException;
import com.dukkan.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private UUID testId;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testProductDTO = ProductDTO.builder()
                .id(testId)
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .categoryId(UUID.randomUUID())
                .isActive(true)
                .build();
    }

    @Test
    void getProductById_ReturnsProduct_WhenExists() throws Exception {
        // Given
        when(productService.getProductById(testId)).thenReturn(testProductDTO);

        // When/Then
        mockMvc.perform(get("/api/v1/products/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(99.99));
    }

    @Test
    void getProductById_ReturnsNotFound_WhenNotExists() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(productService.getProductById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Product", "id", nonExistentId));

        // When/Then
        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createProduct_ReturnsCreated_WhenValid() throws Exception {
        // Given
        ProductDTO newProductDTO = ProductDTO.builder()
                .sku("NEW-SKU-001")
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(20)
                .categoryId(UUID.randomUUID())
                .build();

        ProductDTO savedProductDTO = ProductDTO.builder()
                .id(UUID.randomUUID())
                .sku("NEW-SKU-001")
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(20)
                .categoryId(newProductDTO.getCategoryId())
                .isActive(true)
                .build();

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(savedProductDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("NEW-SKU-001"))
                .andExpect(jsonPath("$.data.name").value("New Product"));
    }

    @Test
    void deleteProduct_ReturnsNoContent_WhenExists() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/products/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
