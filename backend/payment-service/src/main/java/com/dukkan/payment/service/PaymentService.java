package com.dukkan.payment.service;

import com.dukkan.payment.dto.InitiatePaymentRequest;
import com.dukkan.payment.dto.PaymentDTO;
import com.dukkan.payment.dto.RefundPaymentRequest;
import com.dukkan.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for payment operations
 */
public interface PaymentService {

    /**
     * Initiate a new payment using the specified payment provider
     *
     * @param request Payment initiation request
     * @return Payment DTO with payment details
     */
    PaymentDTO initiatePayment(InitiatePaymentRequest request);

    /**
     * Get payment by ID
     *
     * @param id Payment ID
     * @return Payment DTO
     * @throws com.dukkan.payment.exception.PaymentNotFoundException if payment not found
     */
    PaymentDTO getPaymentById(UUID id);

    /**
     * Get payment by reference
     *
     * @param paymentReference Payment reference
     * @return Payment DTO
     * @throws com.dukkan.payment.exception.PaymentNotFoundException if payment not found
     */
    PaymentDTO getPaymentByReference(String paymentReference);

    /**
     * Get all payments for an order
     *
     * @param orderId Order ID
     * @return List of payment DTOs
     */
    List<PaymentDTO> getPaymentsByOrderId(UUID orderId);

    /**
     * Get user payments (paginated)
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of payment DTOs
     */
    Page<PaymentDTO> getUserPayments(UUID userId, Pageable pageable);

    /**
     * Get payments by status (paginated)
     *
     * @param status   Payment status
     * @param pageable Pagination parameters
     * @return Page of payment DTOs
     */
    Page<PaymentDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Refund a payment
     *
     * @param request Refund request
     * @return Updated payment DTO
     * @throws com.dukkan.payment.exception.PaymentNotFoundException       if payment not found
     * @throws com.dukkan.payment.exception.InvalidPaymentStateException if payment cannot be refunded
     */
    PaymentDTO refundPayment(RefundPaymentRequest request);

    /**
     * Get retryable payments for a user (failed or cancelled payments)
     *
     * @param userId User ID
     * @return List of retryable payment DTOs
     */
    List<PaymentDTO> getRetryablePayments(UUID userId);

    /**
     * Handle payment callback from provider (webhook)
     *
     * @param providerTransactionId Provider's transaction ID
     * @param providerResponse      Raw provider response
     * @return Updated payment DTO
     */
    PaymentDTO handlePaymentCallback(String providerTransactionId, String providerResponse);
}
