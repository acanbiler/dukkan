package com.dukkan.email.controller;

import com.dukkan.email.dto.EmailMessageDTO;
import com.dukkan.email.dto.SendEmailRequest;
import com.dukkan.email.model.EmailStatus;
import com.dukkan.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Tag(name = "Emails", description = "Email notification management")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Send an email")
    public ResponseEntity<EmailMessageDTO> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        EmailMessageDTO email = emailService.sendEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(email);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get email by ID")
    public ResponseEntity<EmailMessageDTO> getEmailById(@PathVariable UUID id) {
        EmailMessageDTO email = emailService.getEmailById(id);
        return ResponseEntity.ok(email);
    }

    @GetMapping("/recipient/{email}")
    @Operation(summary = "Get emails by recipient")
    public ResponseEntity<Page<EmailMessageDTO>> getEmailsByRecipient(
        @PathVariable String email,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<EmailMessageDTO> emails = emailService.getEmailsByRecipient(email, page, size);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get emails by status")
    public ResponseEntity<List<EmailMessageDTO>> getEmailsByStatus(@PathVariable EmailStatus status) {
        List<EmailMessageDTO> emails = emailService.getEmailsByStatus(status);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/reference/{referenceId}")
    @Operation(summary = "Get emails by reference ID")
    public ResponseEntity<List<EmailMessageDTO>> getEmailsByReference(
        @PathVariable UUID referenceId,
        @RequestParam String referenceType
    ) {
        List<EmailMessageDTO> emails = emailService.getEmailsByReference(referenceId, referenceType);
        return ResponseEntity.ok(emails);
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed emails")
    public ResponseEntity<Void> retryFailedEmails() {
        emailService.retryFailedEmails();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get email statistics")
    public ResponseEntity<EmailService.EmailStatistics> getStatistics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        EmailService.EmailStatistics stats = emailService.getEmailStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
