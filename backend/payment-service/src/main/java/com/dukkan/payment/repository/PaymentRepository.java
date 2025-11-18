package com.dukkan.payment.repository;

import com.dukkan.payment.model.Payment;
import com.dukkan.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payment by reference
     */
    Optional<Payment> findByPaymentReference(String paymentReference);

    /**
     * Find all payments for an order
     */
    List<Payment> findByOrderId(UUID orderId);

    /**
     * Find all payments by a user (paginated)
     */
    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find payments by status
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Find user payments by status
     */
    Page<Payment> findByUserIdAndStatus(UUID userId, PaymentStatus status, Pageable pageable);

    /**
     * Check if payment reference exists
     */
    boolean existsByPaymentReference(String paymentReference);

    /**
     * Find payments by provider transaction ID
     */
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    /**
     * Find failed payments for retry
     */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status IN ('FAILED', 'CANCELLED') ORDER BY p.createdAt DESC")
    List<Payment> findRetryablePayments(@Param("userId") UUID userId);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    /**
     * Count user payments
     */
    long countByUserId(UUID userId);
}
