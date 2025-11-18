package com.dukkan.email.service.impl;

import com.dukkan.email.dto.EmailMessageDTO;
import com.dukkan.email.dto.SendEmailRequest;
import com.dukkan.email.exception.EmailSendException;
import com.dukkan.email.exception.ResourceNotFoundException;
import com.dukkan.email.mapper.EmailMessageMapper;
import com.dukkan.email.model.EmailMessage;
import com.dukkan.email.model.EmailStatus;
import com.dukkan.email.repository.EmailMessageRepository;
import com.dukkan.email.service.EmailService;
import com.dukkan.email.service.EmailTemplateService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailMessageMapper emailMessageMapper;
    private final EmailTemplateService emailTemplateService;

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    @Override
    @Transactional
    public EmailMessageDTO sendEmail(SendEmailRequest request) {
        log.info("Sending email to: {} of type: {}", request.getRecipientEmail(), request.getEmailType());

        // Check for duplicate emails (optional, can be disabled)
        if (request.getReferenceId() != null &&
            emailMessageRepository.existsByReferenceIdAndReferenceTypeAndEmailType(
                request.getReferenceId(),
                request.getReferenceType(),
                request.getEmailType()
            )) {
            log.warn("Email already sent for reference: {} type: {}", request.getReferenceId(), request.getEmailType());
            // Return existing email instead of sending duplicate
        }

        // Generate email content from template
        String subject = emailTemplateService.generateSubject(
            request.getEmailType(),
            request.getLanguage(),
            request.getTemplateVariables()
        );

        String body = emailTemplateService.generateEmailBody(
            request.getEmailType(),
            request.getLanguage(),
            request.getTemplateVariables()
        );

        // Create email message entity
        EmailMessage emailMessage = EmailMessage.builder()
            .recipientEmail(request.getRecipientEmail())
            .recipientName(request.getRecipientName())
            .subject(subject)
            .body(body)
            .emailType(request.getEmailType())
            .language(request.getLanguage())
            .referenceId(request.getReferenceId())
            .referenceType(request.getReferenceType())
            .status(EmailStatus.PENDING)
            .build();

        // Save to database first
        emailMessage = emailMessageRepository.save(emailMessage);

        // Send via SendGrid
        try {
            String messageId = sendViaSendGrid(emailMessage);
            emailMessage.markAsSent(messageId);
            log.info("Email sent successfully: {} to {}", messageId, request.getRecipientEmail());
        } catch (Exception e) {
            emailMessage.markAsFailed(e.getMessage());
            log.error("Failed to send email: {}", e.getMessage(), e);
            // Don't throw exception - email is saved as failed and can be retried
        }

        // Save updated status
        emailMessage = emailMessageRepository.save(emailMessage);

        return emailMessageMapper.toDTO(emailMessage);
    }

    /**
     * Send email via SendGrid
     */
    private String sendViaSendGrid(EmailMessage emailMessage) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(emailMessage.getRecipientEmail(), emailMessage.getRecipientName());
        Content content = new Content("text/html", emailMessage.getBody());
        Mail mail = new Mail(from, emailMessage.getSubject(), to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            // Extract message ID from headers if available
            String messageId = response.getHeaders().getOrDefault("X-Message-Id", "unknown");
            return messageId;
        } else {
            throw new EmailSendException(
                String.format("SendGrid API error: %d - %s", response.getStatusCode(), response.getBody())
            );
        }
    }

    @Override
    public EmailMessageDTO getEmailById(UUID id) {
        EmailMessage emailMessage = emailMessageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Email", id));
        return emailMessageMapper.toDTO(emailMessage);
    }

    @Override
    public Page<EmailMessageDTO> getEmailsByRecipient(String recipientEmail, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return emailMessageRepository
            .findByRecipientEmailOrderByCreatedAtDesc(recipientEmail, pageable)
            .map(emailMessageMapper::toDTO);
    }

    @Override
    public List<EmailMessageDTO> getEmailsByStatus(EmailStatus status) {
        return emailMessageRepository.findByStatus(status)
            .stream()
            .map(emailMessageMapper::toDTO)
            .toList();
    }

    @Override
    public List<EmailMessageDTO> getEmailsByReference(UUID referenceId, String referenceType) {
        return emailMessageRepository.findByReferenceIdAndReferenceType(referenceId, referenceType)
            .stream()
            .map(emailMessageMapper::toDTO)
            .toList();
    }

    @Override
    @Transactional
    public void retryFailedEmails() {
        List<EmailMessage> retryableEmails = emailMessageRepository
            .findRetryableEmails(EmailStatus.FAILED, 3);

        log.info("Retrying {} failed emails", retryableEmails.size());

        for (EmailMessage emailMessage : retryableEmails) {
            try {
                emailMessage.setStatus(EmailStatus.SENDING);
                String messageId = sendViaSendGrid(emailMessage);
                emailMessage.markAsSent(messageId);
                log.info("Retry successful for email: {}", emailMessage.getId());
            } catch (Exception e) {
                emailMessage.markAsFailed(e.getMessage());
                log.error("Retry failed for email: {}", emailMessage.getId(), e);
            }
            emailMessageRepository.save(emailMessage);
        }
    }

    @Override
    public EmailStatistics getEmailStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<EmailMessage> emails = emailMessageRepository.findEmailsSentBetween(startDate, endDate);

        long totalSent = emails.stream().filter(e -> e.getStatus() == EmailStatus.SENT).count();
        long totalFailed = emails.stream().filter(e -> e.getStatus() == EmailStatus.FAILED).count();
        long totalDelivered = emails.stream().filter(e -> e.getStatus() == EmailStatus.DELIVERED).count();
        long pendingCount = emailMessageRepository.countByStatus(EmailStatus.PENDING);

        return new EmailStatistics(totalSent, totalFailed, totalDelivered, pendingCount);
    }
}
