package com.dukkan.payment.controller;

import com.dukkan.payment.dto.ApiResponse;
import com.dukkan.payment.dto.InitiatePaymentRequest;
import com.dukkan.payment.dto.PaymentDTO;
import com.dukkan.payment.dto.RefundPaymentRequest;
import com.dukkan.payment.model.PaymentStatus;
import com.dukkan.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for payment operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Initiate a new payment",
            description = "Create a new payment and process it through the specified payment provider"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Payment processing failed"
            )
    })
    public ResponseEntity<ApiResponse<PaymentDTO>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Received payment initiation request for order: {}", request.getOrderId());
        PaymentDTO payment = paymentService.initiatePayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment initiated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieve payment details by payment ID"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable UUID id) {
        log.info("Fetching payment by ID: {}", id);
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/reference/{reference}")
    @Operation(
            summary = "Get payment by reference",
            description = "Retrieve payment details by payment reference"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByReference(
            @Parameter(description = "Payment reference") @PathVariable String reference) {
        log.info("Fetching payment by reference: {}", reference);
        PaymentDTO payment = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
            summary = "Get payments by order ID",
            description = "Retrieve all payments associated with an order"
    )
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByOrderId(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        log.info("Fetching payments for order: {}", orderId);
        List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user payments (paginated)",
            description = "Retrieve all payments for a user with pagination"
    )
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getUserPayments(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Fetching payments for user: {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentDTO> payments = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get payments by status (paginated)",
            description = "Retrieve all payments with a specific status"
    )
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching payments with status: {} (page: {}, size: {})", status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentDTO> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/refund")
    @Operation(
            summary = "Refund a payment",
            description = "Process a full or partial refund for a completed payment"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Refund processed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid refund request or payment state"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentDTO>> refundPayment(
            @Valid @RequestBody RefundPaymentRequest request) {
        log.info("Processing refund for payment: {}", request.getPaymentReference());
        PaymentDTO payment = paymentService.refundPayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Refund processed successfully"));
    }

    @GetMapping("/retryable/{userId}")
    @Operation(
            summary = "Get retryable payments",
            description = "Retrieve failed or cancelled payments that can be retried"
    )
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getRetryablePayments(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching retryable payments for user: {}", userId);
        List<PaymentDTO> payments = paymentService.getRetryablePayments(userId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/callback")
    @Operation(
            summary = "Handle payment provider callback",
            description = "Webhook endpoint for payment provider callbacks (internal use)"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> handleCallback(
            @Parameter(description = "Provider transaction ID") @RequestParam String transactionId,
            @Parameter(description = "Provider response") @RequestBody String providerResponse) {
        log.info("Received payment callback for transaction: {}", transactionId);
        PaymentDTO payment = paymentService.handlePaymentCallback(transactionId, providerResponse);
        return ResponseEntity.ok(ApiResponse.success(payment, "Callback processed"));
    }
}
