package com.dukkan.user.repository;

import com.dukkan.user.model.Role;
import com.dukkan.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
        assertThat(found.get().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_ShouldBeCaseExact() {
        // Given
        User user = User.builder()
                .email("Test@Example.com")
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("Test@Example.com");
        Optional<User> notFound = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Given
        User user = User.builder()
                .email("existing@example.com")
                .passwordHash("encoded-password")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldGenerateIdAutomatically() {
        // Given
        User user = User.builder()
                .email("newuser@example.com")
                .passwordHash("encoded-password")
                .firstName("New")
                .lastName("User")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_WithAdminRole_ShouldPersistCorrectly() {
        // Given
        User admin = User.builder()
                .email("admin@example.com")
                .passwordHash("encoded-password")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .isActive(true)
                .emailVerified(true)
                .build();

        // When
        User saved = userRepository.save(admin);

        // Then
        Optional<User> found = userRepository.findByEmail("admin@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(found.get().isAdmin()).isTrue();
        assertThat(found.get().isCustomer()).isFalse();
    }

    @Test
    void save_ShouldEnforceEmailUniqueness() {
        // Given
        User user1 = User.builder()
                .email("duplicate@example.com")
                .passwordHash("password1")
                .firstName("User")
                .lastName("One")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .email("duplicate@example.com")
                .passwordHash("password2")
                .firstName("User")
                .lastName("Two")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();

        // When / Then
        // H2 will throw a DataIntegrityViolationException due to unique constraint
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> {
                    userRepository.save(user2);
                    userRepository.flush(); // Force immediate database operation
                }
        );
    }
}
