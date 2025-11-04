//package com.example.notificationservice.business.interfaces;
//
//import com.example.notificationservice.domain.dto.PropertyDto;
//
///**
// * Email Service Interface
// * Handles email sending operations
// */
//public interface EmailService {
//
//    /**
//     * Send plain text email
//     */
//    void sendEmail(String to, String subject, String body);
//
//    /**
//     * Send HTML email
//     */
//    void sendHtmlEmail(String to, String subject, String htmlContent);
//
//    /**
//     * Send new property notification email
//     */
//    void sendNewPropertyNotification(String toEmail, String userName, PropertyDto property);
//
//    /**
//     * Send welcome email
//     */
//    void sendWelcomeEmail(String toEmail, String userName);
//
//    /**
//     * Send appointment confirmation email
//     */
//    void sendAppointmentConfirmation(String toEmail, String userName, String appointmentDetails);
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
// TODO: implement email service
package com.example.notificationservice.business.interfaces;

import com.example.notificationservice.domain.dto.PropertyDto;

/**
 * Email Service Interface
 * Handles email sending operations via Mailpit
 * View emails at: http://localhost:8025
 */
public interface EmailService {


    void sendEmail(String to, String subject, String body);


    void sendHtmlEmail(String to, String subject, String htmlContent);


    void sendNewPropertyNotification(String toEmail, String userName, PropertyDto property);

    /**
     * Send welcome email to new users
     * @param toEmail Recipient email address
     * @param userName User's name
     */
    void sendWelcomeEmail(String toEmail, String userName);

    /**
     * Send appointment confirmation email
     * @param toEmail Recipient email address
     * @param userName User's name
     * @param appointmentDetails Details of the appointment
     */
    void sendAppointmentConfirmation(String toEmail, String userName, String appointmentDetails);

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
