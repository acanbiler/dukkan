package com.dukkan.email.service;

import com.dukkan.email.model.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service for generating email content from templates.
 * Supports internationalization (EN/TR).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    /**
     * Generate email subject based on type and language
     */
    public String generateSubject(EmailType emailType, String language, Map<String, Object> variables) {
        return switch (emailType) {
            case WELCOME -> language.equals("tr") ? "Dukkan'a Hoş Geldiniz!" : "Welcome to Dukkan!";
            case ORDER_CONFIRMATION -> language.equals("tr") ?
                "Siparişiniz Alındı - #" + variables.getOrDefault("orderNumber", "") :
                "Order Confirmation - #" + variables.getOrDefault("orderNumber", "");
            case PAYMENT_SUCCESS -> language.equals("tr") ?
                "Ödemeniz Başarıyla Alındı" :
                "Payment Successful";
            case PAYMENT_FAILED -> language.equals("tr") ?
                "Ödeme Başarısız" :
                "Payment Failed";
            case ORDER_SHIPPED -> language.equals("tr") ?
                "Siparişiniz Kargoya Verildi" :
                "Your Order Has Been Shipped";
            case ORDER_DELIVERED -> language.equals("tr") ?
                "Siparişiniz Teslim Edildi" :
                "Your Order Has Been Delivered";
            case ORDER_CANCELLED -> language.equals("tr") ?
                "Siparişiniz İptal Edildi" :
                "Your Order Has Been Cancelled";
            case PASSWORD_RESET -> language.equals("tr") ?
                "Şifre Sıfırlama Talebi" :
                "Password Reset Request";
            case EMAIL_VERIFICATION -> language.equals("tr") ?
                "Email Adresinizi Doğrulayın" :
                "Verify Your Email Address";
        };
    }

    /**
     * Generate email body from Thymeleaf template
     */
    public String generateEmailBody(EmailType emailType, String language, Map<String, Object> variables) {
        String templateName = getTemplateName(emailType, language);
        Context context = new Context();

        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        // Add common variables
        context.setVariable("year", java.time.Year.now().getValue());
        context.setVariable("language", language);

        try {
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.error("Error processing template: {}", templateName, e);
            // Fallback to simple text email
            return generateFallbackEmail(emailType, language, variables);
        }
    }

    /**
     * Get template file name based on email type and language
     */
    private String getTemplateName(EmailType emailType, String language) {
        String baseName = emailType.name().toLowerCase().replace('_', '-');
        return String.format("email/%s-%s", baseName, language);
    }

    /**
     * Generate simple fallback email if template fails
     */
    private String generateFallbackEmail(EmailType emailType, String language, Map<String, Object> variables) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>").append(generateSubject(emailType, language, variables)).append("</h2>");
        html.append("<p>").append(language.equals("tr") ?
            "Bu bir otomatik mesajdır." :
            "This is an automated message.").append("</p>");
        html.append("</body></html>");
        return html.toString();
    }
}
