package com.dukkan.email.dto;

import com.dukkan.email.model.EmailType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for sending an email request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    private String recipientName;

    @NotNull(message = "Email type is required")
    private EmailType emailType;

    /**
     * Language code for the email template (e.g., "en", "tr")
     */
    @Builder.Default
    private String language = "en";

    /**
     * Reference ID to related entity (e.g., order ID, payment ID)
     */
    private UUID referenceId;

    /**
     * Reference type (e.g., "order", "payment", "user")
     */
    private String referenceType;

    /**
     * Template variables to populate the email content.
     * Keys depend on the email type.
     * Example for ORDER_CONFIRMATION:
     * {
     *   "orderNumber": "ORD-123456",
     *   "totalAmount": "150.00",
     *   "items": [...]
     * }
     */
    private Map<String, Object> templateVariables;
}
