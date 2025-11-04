package com.example.notificationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Email-specific message payload
 * Used for simple email notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class EmailMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String to;
    private String subject;
    private String body;
    private String htmlContent;
    private Boolean isHtml;
    private LocalDateTime timestamp;
}
