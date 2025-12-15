package com.example.notificationservice.business.interfaces;

import com.example.notificationservice.domain.dto.PropertyDto;
import java.time.LocalDateTime;
import java.math.BigDecimal;  // ← ADD THIS IMPORT
/**
 * Email Service Interface
 * Handles email sending operations via Mailpit
 * View emails at: http://localhost:8025
 */
public interface EmailService {

    /**
     * Send plain text email
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send HTML email
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Send new property notification email with full HTML template
     */
    void sendNewPropertyNotification(String toEmail, String userName, PropertyDto property);

    /**
     * Send welcome email to new users
     * @param toEmail Recipient email address
     * @param userName User's name
     */
    void sendWelcomeEmail(String toEmail, String userName);

    /**
     * Send appointment confirmation email (simple version)
     * @param toEmail Recipient email address
     * @param userName User's name
     * @param appointmentDetails Details of the appointment
     */
    //void sendAppointmentConfirmation(String toEmail, String userName, String appointmentDetails);

    /**
     * 🆕 Send beautiful HTML appointment confirmed email (for SAGA events)
     * Uses appointment-confirmed-email.html template with full styling
     * @param toEmail Recipient email address
     * @param userName User's name
     * @param appointmentDateTime Date and time of appointment
     * @param durationMinutes Duration in minutes
     * @param propertyTitle Property title
     * @param propertyAddress Property address
     * @param propertyPrice Monthly rent price
     * @param propertyImageUrl Property image URL
     * @param providerName Property manager's name
     * @param providerEmail Property manager's email
     * @param providerPhone Property manager's phone
     * @param propertyUrl Link to view property details
     * @param appointmentUrl Link to manage appointment
     */
    void sendAppointmentConfirmedEmail(
            String toEmail,
            String userName,
            LocalDateTime appointmentDateTime,
            Integer durationMinutes,
            String propertyTitle,
            String propertyAddress,
            BigDecimal propertyPrice,  // ← CHANGED FROM Double TO BigDecimal
            //Double propertyPrice,
            String propertyImageUrl,
            String providerName,
            String providerEmail,
            String providerPhone,
            String propertyUrl,
            String appointmentUrl
    );

    /**
     * Send magic link for passwordless authentication
     * @param toEmail Recipient email address
     * @param magicLink The magic link URL
     * @param code Alternative verification code
     */
    void sendMagicLinkEmail(String toEmail, String magicLink, String code);

    /**
     * Send password reset email
     * @param toEmail Recipient email address
     * @param userName User's name
     * @param resetLink Password reset link
     * @param code Alternative reset code
     */
    void sendPasswordResetEmail(String toEmail, String userName, String resetLink, String code);

    /**
     * Send email verification link
     * @param toEmail Recipient email address
     * @param userName User's name
     * @param verificationLink Email verification link
     * @param code Alternative verification code
     */
    void sendVerificationEmail(String toEmail, String userName, String verificationLink, String code);
}