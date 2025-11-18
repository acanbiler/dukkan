package com.dukkan.payment.provider.impl;

import com.dukkan.payment.config.PaymentProviderProperties;
import com.dukkan.payment.exception.PaymentProcessingException;
import com.dukkan.payment.model.PaymentMethod;
import com.dukkan.payment.model.PaymentProvider;
import com.dukkan.payment.provider.PaymentProviderService;
import com.dukkan.payment.provider.PaymentRequest;
import com.dukkan.payment.provider.PaymentResponse;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.CreateRefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Iyzico payment provider implementation
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "payment.providers.iyzico", name = "enabled", havingValue = "true")
public class IyzicoPaymentProvider implements PaymentProviderService {

    private final Options options;
    private final PaymentProviderProperties.IyzicoProperties iyzicoProperties;

    public IyzicoPaymentProvider(PaymentProviderProperties properties) {
        this.iyzicoProperties = properties.getIyzico();
        this.options = new Options();
        this.options.setApiKey(iyzicoProperties.getApiKey());
        this.options.setSecretKey(iyzicoProperties.getSecretKey());
        this.options.setBaseUrl(iyzicoProperties.getBaseUrl());
        log.info("Initialized Iyzico payment provider with base URL: {}", iyzicoProperties.getBaseUrl());
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing Iyzico payment for reference: {}", request.getPaymentReference());

        try {
            // Build Iyzico payment request
            CreatePaymentRequest iyzicoRequest = buildIyzicoPaymentRequest(request);

            // Execute payment
            Payment payment = Payment.create(iyzicoRequest, options);

            // Build response
            return buildPaymentResponse(payment);

        } catch (Exception e) {
            log.error("Error processing Iyzico payment: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process Iyzico payment", e);
        }
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing Iyzico refund for transaction: {}, amount: {}", transactionId, amount);

        try {
            // Build refund request
            CreateRefundRequest refundRequest = new CreateRefundRequest();
            refundRequest.setLocale(Locale.TR.getValue());
            refundRequest.setConversationId(UUID.randomUUID().toString());
            refundRequest.setPaymentTransactionId(transactionId);
            refundRequest.setPrice(amount);
            refundRequest.setCurrency(Currency.TRY.name());
            refundRequest.setIp("127.0.0.1"); // Will be updated with actual IP

            // Execute refund
            Refund refund = Refund.create(refundRequest, options);

            // Build response
            return PaymentResponse.builder()
                    .success(Status.SUCCESS.getValue().equals(refund.getStatus()))
                    .transactionId(transactionId)
                    .status(refund.getStatus())
                    .message(refund.getStatus())
                    .errorCode(refund.getErrorCode())
                    .errorMessage(refund.getErrorMessage())
                    .providerRawResponse(refund.toString())
                    .build();

        } catch (Exception e) {
            log.error("Error processing Iyzico refund: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process Iyzico refund", e);
        }
    }

    @Override
    public PaymentProvider getProviderName() {
        return PaymentProvider.IYZICO;
    }

    @Override
    public boolean supportsPaymentMethod(PaymentMethod method) {
        // Iyzico supports credit and debit cards
        return method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD;
    }

    /**
     * Build Iyzico payment request from our generic payment request
     */
    private CreatePaymentRequest buildIyzicoPaymentRequest(PaymentRequest request) {
        CreatePaymentRequest iyzicoRequest = new CreatePaymentRequest();

        // Basic payment information
        iyzicoRequest.setLocale(Locale.TR.getValue());
        iyzicoRequest.setConversationId(request.getPaymentReference());
        iyzicoRequest.setPrice(request.getAmount());
        iyzicoRequest.setPaidPrice(request.getAmount()); // No installment fees for MVP
        iyzicoRequest.setCurrency(Currency.valueOf(request.getCurrency()).name());
        iyzicoRequest.setInstallment(1); // Single payment for MVP
        iyzicoRequest.setBasketId(UUID.randomUUID().toString().substring(0, 8));
        iyzicoRequest.setPaymentChannel(PaymentChannel.WEB.name());
        iyzicoRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        // Payment card
        if (request.getCardDetails() != null) {
            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setCardHolderName(request.getCardDetails().getCardHolderName());
            paymentCard.setCardNumber(request.getCardDetails().getCardNumber());
            paymentCard.setExpireMonth(request.getCardDetails().getExpireMonth());
            paymentCard.setExpireYear(request.getCardDetails().getExpireYear());
            paymentCard.setCvc(request.getCardDetails().getCvc());
            iyzicoRequest.setPaymentCard(paymentCard);
        }

        // Buyer information
        Buyer buyer = new Buyer();
        buyer.setId(UUID.randomUUID().toString().substring(0, 8));
        buyer.setName(extractFirstName(request.getBillingAddress().getContactName()));
        buyer.setSurname(extractLastName(request.getBillingAddress().getContactName()));
        buyer.setEmail(request.getCustomerEmail());
        buyer.setGsmNumber("+905350000000"); // Default for MVP - will be updated with actual phone
        buyer.setIdentityNumber("11111111111"); // Default for MVP - will be updated with actual ID
        buyer.setRegistrationAddress(request.getBillingAddress().getAddress());
        buyer.setIp(request.getIpAddress() != null ? request.getIpAddress() : "127.0.0.1");
        buyer.setCity(request.getBillingAddress().getCity());
        buyer.setCountry(request.getBillingAddress().getCountry());
        buyer.setZipCode(request.getBillingAddress().getZipCode());
        iyzicoRequest.setBuyer(buyer);

        // Shipping address (same as billing for MVP)
        Address shippingAddress = new Address();
        shippingAddress.setContactName(request.getBillingAddress().getContactName());
        shippingAddress.setCity(request.getBillingAddress().getCity());
        shippingAddress.setCountry(request.getBillingAddress().getCountry());
        shippingAddress.setAddress(request.getBillingAddress().getAddress());
        shippingAddress.setZipCode(request.getBillingAddress().getZipCode());
        iyzicoRequest.setShippingAddress(shippingAddress);

        // Billing address
        Address billingAddress = new Address();
        billingAddress.setContactName(request.getBillingAddress().getContactName());
        billingAddress.setCity(request.getBillingAddress().getCity());
        billingAddress.setCountry(request.getBillingAddress().getCountry());
        billingAddress.setAddress(request.getBillingAddress().getAddress());
        billingAddress.setZipCode(request.getBillingAddress().getZipCode());
        iyzicoRequest.setBillingAddress(billingAddress);

        // Basket items (single item for MVP)
        List<BasketItem> basketItems = new ArrayList<>();
        BasketItem basketItem = new BasketItem();
        basketItem.setId("ITEM1");
        basketItem.setName("Order Payment");
        basketItem.setCategory1("General");
        basketItem.setItemType(BasketItemType.PHYSICAL.name());
        basketItem.setPrice(request.getAmount());
        basketItems.add(basketItem);
        iyzicoRequest.setBasketItems(basketItems);

        return iyzicoRequest;
    }

    /**
     * Build payment response from Iyzico payment result
     */
    private PaymentResponse buildPaymentResponse(Payment payment) {
        boolean isSuccess = Status.SUCCESS.getValue().equals(payment.getStatus());

        PaymentResponse.PaymentResponseBuilder responseBuilder = PaymentResponse.builder()
                .success(isSuccess)
                .status(payment.getStatus())
                .providerRawResponse(payment.toString());

        if (isSuccess) {
            // Extract payment transaction ID (Iyzico returns this in payment items)
            String transactionId = payment.getPaymentId();
            if (payment.getPaymentItems() != null && !payment.getPaymentItems().isEmpty()) {
                transactionId = payment.getPaymentItems().get(0).getPaymentTransactionId();
            }

            responseBuilder
                    .transactionId(transactionId)
                    .message("Payment completed successfully");
        } else {
            responseBuilder
                    .errorCode(payment.getErrorCode())
                    .errorMessage(payment.getErrorMessage())
                    .message("Payment failed: " + payment.getErrorMessage());
        }

        return responseBuilder.build();
    }

    /**
     * Extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extract last name from full name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "Unknown";
    }
}
