package com.dukkan.user.service;

import com.dukkan.user.dto.AuthResponse;
import com.dukkan.user.dto.LoginRequest;
import com.dukkan.user.dto.RegisterRequest;
import com.dukkan.user.model.Role;
import com.dukkan.user.model.User;
import com.dukkan.user.repository.UserRepository;
import com.dukkan.user.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WithValidData_ShouldSucceed() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("test-jwt-token");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(response.getLastName()).isEqualTo(request.getLastName());
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(savedUser.getId(), savedUser.getEmail(), "CUSTOMER");
    }

    @Test
    void register_WithDuplicateEmail_ShouldThrowException() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldEncodePassword() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("plain-password")
                .firstName("John")
                .lastName("Doe")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("test-token");

        // When
        authService.register(request);

        // Then
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void register_ShouldSetDefaultRoleToCustomer() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("test-token");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void login_WithValidCredentials_ShouldSucceed() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(user.getId(), user.getEmail(), "CUSTOMER"))
                .thenReturn("test-jwt-token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getUserId()).isEqualTo(user.getId());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPasswordHash());
        verify(jwtUtil).generateToken(user.getId(), user.getEmail(), "CUSTOMER");
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowBadCredentialsException() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrong-password")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPasswordHash());
        verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    void login_WithInactiveUser_ShouldThrowBadCredentialsException() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .isActive(false) // Inactive user
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(inactiveUser));

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Account is inactive");

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldGenerateJwtToken() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(request.getEmail())
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ADMIN) // Test with ADMIN role
                .isActive(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(userId, user.getEmail(), "ADMIN"))
                .thenReturn("admin-jwt-token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response.getToken()).isEqualTo("admin-jwt-token");
        verify(jwtUtil).generateToken(userId, user.getEmail(), "ADMIN");
    }
}
