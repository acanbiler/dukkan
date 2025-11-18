package com.dukkan.payment.service.impl;

import com.dukkan.payment.dto.InitiatePaymentRequest;
import com.dukkan.payment.dto.PaymentDTO;
import com.dukkan.payment.dto.RefundPaymentRequest;
import com.dukkan.payment.exception.InvalidPaymentStateException;
import com.dukkan.payment.exception.PaymentNotFoundException;
import com.dukkan.payment.exception.PaymentProcessingException;
import com.dukkan.payment.exception.PaymentProviderNotSupportedException;
import com.dukkan.payment.mapper.PaymentMapper;
import com.dukkan.payment.model.Payment;
import com.dukkan.payment.model.PaymentProvider;
import com.dukkan.payment.model.PaymentStatus;
import com.dukkan.payment.provider.PaymentProviderService;
import com.dukkan.payment.provider.PaymentRequest;
import com.dukkan.payment.provider.PaymentResponse;
import com.dukkan.payment.repository.PaymentRepository;
import com.dukkan.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentService with provider delegation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final Map<PaymentProvider, PaymentProviderService> paymentProviders;

    @Override
    @Transactional
    public PaymentDTO initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment for order {} with provider {}", request.getOrderId(), request.getProvider());

        // Validate provider is supported
        PaymentProviderService provider = getProvider(request.getProvider());

        // Validate payment method is supported by provider
        if (!provider.supportsPaymentMethod(request.getMethod())) {
            throw new PaymentProcessingException(
                    String.format("Payment method %s is not supported by provider %s",
                            request.getMethod(), request.getProvider())
            );
        }

        // Create payment entity
        Payment payment = paymentMapper.toEntity(request);
        payment = paymentRepository.save(payment);

        // Mark as processing
        payment.markAsProcessing();
        payment = paymentRepository.save(payment);

        try {
            // Build provider request
            PaymentRequest providerRequest = PaymentRequest.builder()
                    .paymentReference(payment.getPaymentReference())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .method(request.getMethod())
                    .customerEmail(request.getCustomerEmail())
                    .cardDetails(request.getCardDetails())
                    .billingAddress(request.getBillingAddress())
                    .callbackUrl(request.getCallbackUrl())
                    .ipAddress(request.getIpAddress())
                    .build();

            // Delegate to payment provider
            PaymentResponse providerResponse = provider.processPayment(providerRequest);

            // Update payment based on provider response
            if (providerResponse.isSuccess()) {
                payment.complete(providerResponse.getTransactionId(), providerResponse.getProviderRawResponse());
                log.info("Payment {} completed successfully with transaction ID {}",
                        payment.getPaymentReference(), providerResponse.getTransactionId());
            } else {
                String errorMessage = providerResponse.getErrorMessage() != null
                        ? providerResponse.getErrorMessage()
                        : "Payment failed";
                payment.fail(errorMessage, providerResponse.getProviderRawResponse());
                log.warn("Payment {} failed: {}", payment.getPaymentReference(), errorMessage);
            }

            payment = paymentRepository.save(payment);
            return paymentMapper.toDTO(payment);

        } catch (Exception e) {
            log.error("Error processing payment {}: {}", payment.getPaymentReference(), e.getMessage(), e);
            payment.fail("Payment processing error: " + e.getMessage(), null);
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Failed to process payment", e);
        }
    }

    @Override
    public PaymentDTO getPaymentById(UUID id) {
        log.debug("Fetching payment by ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByReference(String paymentReference) {
        log.debug("Fetching payment by reference: {}", paymentReference);
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public List<PaymentDTO> getPaymentsByOrderId(UUID orderId) {
        log.debug("Fetching payments for order: {}", orderId);
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PaymentDTO> getUserPayments(UUID userId, Pageable pageable) {
        log.debug("Fetching payments for user: {}", userId);
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(paymentMapper::toDTO);
    }

    @Override
    public Page<PaymentDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Fetching payments with status: {}", status);
        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        return payments.map(paymentMapper::toDTO);
    }

    @Override
    @Transactional
    public PaymentDTO refundPayment(RefundPaymentRequest request) {
        log.info("Processing refund for payment {}", request.getPaymentReference());

        // Find payment
        Payment payment = paymentRepository.findByPaymentReference(request.getPaymentReference())
                .orElseThrow(() -> new PaymentNotFoundException(request.getPaymentReference()));

        // Validate payment can be refunded
        if (!payment.isSuccessful()) {
            throw new InvalidPaymentStateException(
                    "Cannot refund payment with status: " + payment.getStatus()
            );
        }

        // Validate refund amount
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new InvalidPaymentStateException(
                    "Refund amount cannot exceed original payment amount"
            );
        }

        // Get provider
        PaymentProviderService provider = getProvider(payment.getProvider());

        try {
            // Call provider refund
            PaymentResponse providerResponse = provider.refundPayment(
                    payment.getProviderTransactionId(),
                    request.getAmount()
            );

            // Update payment status
            if (providerResponse.isSuccess()) {
                if (request.getAmount().compareTo(payment.getAmount()) == 0) {
                    payment.refund(); // Full refund
                } else {
                    payment.setStatus(PaymentStatus.PARTIAL_REFUND); // Partial refund
                }
                payment.setFailureReason(request.getReason());
                log.info("Refund processed successfully for payment {}", payment.getPaymentReference());
            } else {
                throw new PaymentProcessingException(
                        "Refund failed: " + providerResponse.getErrorMessage()
                );
            }

            payment = paymentRepository.save(payment);
            return paymentMapper.toDTO(payment);

        } catch (Exception e) {
            log.error("Error processing refund for payment {}: {}",
                    payment.getPaymentReference(), e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process refund", e);
        }
    }

    @Override
    public List<PaymentDTO> getRetryablePayments(UUID userId) {
        log.debug("Fetching retryable payments for user: {}", userId);
        List<Payment> payments = paymentRepository.findRetryablePayments(userId);
        return payments.stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentDTO handlePaymentCallback(String providerTransactionId, String providerResponse) {
        log.info("Handling payment callback for transaction: {}", providerTransactionId);

        Payment payment = paymentRepository.findByProviderTransactionId(providerTransactionId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with provider transaction ID: " + providerTransactionId
                ));

        // Update payment status based on callback
        // This is a simplified version - actual implementation depends on provider callback format
        if (providerResponse.contains("SUCCESS") || providerResponse.contains("COMPLETED")) {
            if (payment.getStatus() == PaymentStatus.PROCESSING) {
                payment.complete(providerTransactionId, providerResponse);
            }
        } else if (providerResponse.contains("FAILED")) {
            if (payment.getStatus() == PaymentStatus.PROCESSING) {
                payment.fail("Payment failed from provider callback", providerResponse);
            }
        }

        payment = paymentRepository.save(payment);
        return paymentMapper.toDTO(payment);
    }

    /**
     * Get payment provider service by provider enum
     *
     * @param provider Payment provider enum
     * @return PaymentProviderService implementation
     * @throws PaymentProviderNotSupportedException if provider not configured
     */
    private PaymentProviderService getProvider(PaymentProvider provider) {
        PaymentProviderService providerService = paymentProviders.get(provider);
        if (providerService == null) {
            throw new PaymentProviderNotSupportedException(provider);
        }
        return providerService;
    }
}
