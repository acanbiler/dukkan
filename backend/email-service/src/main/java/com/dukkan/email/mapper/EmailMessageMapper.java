package com.dukkan.email.mapper;

import com.dukkan.email.dto.EmailMessageDTO;
import com.dukkan.email.model.EmailMessage;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between EmailMessage entity and DTOs.
 * Following Dukkan's manual mapping approach (no MapStruct).
 */
@Component
public class EmailMessageMapper {

    /**
     * Convert EmailMessage entity to DTO
     */
    public EmailMessageDTO toDTO(EmailMessage entity) {
        if (entity == null) {
            return null;
        }

        return new EmailMessageDTO(
            entity.getId(),
            entity.getRecipientEmail(),
            entity.getRecipientName(),
            entity.getSubject(),
            entity.getEmailType(),
            entity.getStatus(),
            entity.getLanguage(),
            entity.getReferenceId(),
            entity.getReferenceType(),
            entity.getSendAttempts(),
            entity.getErrorMessage(),
            entity.getSentAt(),
            entity.getDeliveredAt(),
            entity.getCreatedAt()
        );
    }
}
