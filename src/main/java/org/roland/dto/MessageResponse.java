package org.roland.dto;

import java.time.LocalDateTime;

public record MessageResponse(Long id, String content, LocalDateTime timestamp) {
    
    public static MessageResponse fromEntity(org.roland.model.Message message) {
        return new MessageResponse(message.getId(), message.getContent(), message.getTimestamp());
    }
}
