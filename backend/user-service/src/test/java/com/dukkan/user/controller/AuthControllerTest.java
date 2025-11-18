package com.dukkan.user.controller;

import com.dukkan.user.dto.AuthResponse;
import com.dukkan.user.dto.LoginRequest;
import com.dukkan.user.dto.RegisterRequest;
import com.dukkan.user.model.Role;
import com.dukkan.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API tests for AuthController
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_WithValidRequest_ShouldReturn201() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("test-jwt-token")
                .type("Bearer")
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.CUSTOMER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_WithDuplicateEmail_ShouldReturn400() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithMissingFields_ShouldReturn400() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                // Missing password, firstName, lastName
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("login-jwt-token")
                .type("Bearer")
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("wrong-password")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithMissingEmail_ShouldReturn400() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .password("password123")
                // Missing email
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithMissingPassword_ShouldReturn400() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                // Missing password
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithInactiveAccount_ShouldReturn401() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("inactive@example.com")
                .password("password123")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Account is inactive"));

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
