package com.example.notificationservice.business.impl;

import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.domain.dto.PropertyDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

///
//  Email Service Implementation
//  Handles email sending via Mailpit SMTP
//  Mailpit Web UI: http://localhost:8025
//
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.email.enabled:true}")
    private Boolean emailEnabled;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.warn(" Email sending is disabled. Skipping email to: {}", to);
            return;
        }

        try {
            log.info(" Sending plain text email to: {} | Subject: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info(" Email sent successfully to: {}", to);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the email");

        } catch (Exception e) {
            log.error(" Failed to send email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.warn("Email sending is disabled. Skipping HTML email to: {}", to);
            return;
        }

        try {
            log.info(" Sending HTML email to: {} | Subject: {}", to, subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info(" HTML email sent successfully to: {}", to);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the email");

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(" Failed to send HTML email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendNewPropertyNotification(String toEmail, String userName, PropertyDto property) {
        log.info("Async: Sending new property notification to: {} for property: {}", toEmail, property.getTitle());

        try {
            // Create Thymeleaf context with property data
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("propertyTitle", property.getTitle());
            context.setVariable("propertyAddress", property.getAddress());
            context.setVariable("propertyPrice", property.getRentAmount());
            context.setVariable("propertyBedrooms", property.getBedrooms() != null ? property.getBedrooms() : 0);
            context.setVariable("propertyArea", property.getSurfaceArea() != null ? property.getSurfaceArea() : "N/A");

            // Handle property images - check both getImages() and getImage()
            String imageUrl = "https://via.placeholder.com/600x400?text=Property+Image";
            if (property.getImages() != null && !property.getImages().isEmpty()) {
                imageUrl = property.getImages().get(0);
            } else if (property.getImage() != null && !property.getImage().isEmpty()) {
                imageUrl = property.getImage();
            }
            context.setVariable("propertyImageUrl", imageUrl);

            // Property URL for viewing details
            context.setVariable("propertyUrl", "http://localhost:5173/property/" + property.getId());

            // Process email template
            String htmlContent = templateEngine.process("new-property-email", context);

            // Send email
            sendHtmlEmail(toEmail,
                    " New Property Available: " + property.getTitle(),
                    htmlContent);

            log.info(" New property notification sent successfully to: {}", toEmail);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the notification");

        } catch (Exception e) {
            log.error(" Failed to send new property notification to: {}. Error: {}",
                    toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send new property notification", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName) {
        log.info(" Sending welcome email to: {} (User: {})", toEmail, userName);

        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("platformName", "Property Rental Platform");
            context.setVariable("dashboardUrl", "http://localhost:5173/dashboard");

            String htmlContent = templateEngine.process("welcome-email", context);
            sendHtmlEmail(toEmail, "Welcome to Property Rental Platform! ", htmlContent);

            log.info(" Welcome email sent successfully to: {}", toEmail);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the welcome email");

        } catch (Exception e) {
            log.error(" Failed to send welcome email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Override
    public void sendAppointmentConfirmation(String toEmail, String userName, String appointmentDetails) {
        log.info(" Sending appointment confirmation to: {} (User: {})", toEmail, userName);

        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("appointmentDetails", appointmentDetails);
            context.setVariable("appointmentsUrl", "http://localhost:5173/appointments");

            String htmlContent = templateEngine.process("appointment-confirmation", context);
            sendHtmlEmail(toEmail, "Appointment Confirmed ", htmlContent);

            log.info("Appointment confirmation sent successfully to: {}", toEmail);
            log.info("Check Mailpit UI at http://localhost:8025 to view the confirmation");

        } catch (Exception e) {
            log.error(" Failed to send appointment confirmation to: {}. Error: {}",
                    toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send appointment confirmation", e);
        }
    }

    @Override
    public void sendMagicLinkEmail(String toEmail, String magicLink, String code) {
        log.info(" Sending magic link email to: {}", toEmail);

        try {
            Context context = new Context();
            context.setVariable("magicLink", magicLink);
            context.setVariable("code", code);
            context.setVariable("expirationMinutes", 15);

            String htmlContent = templateEngine.process("magic-link-email", context);
            sendHtmlEmail(toEmail, "Your Magic Link ", htmlContent);

            log.info(" Magic link email sent successfully to: {}", toEmail);
            log.info(" Magic Link: {}", magicLink);
            log.info(" Verification Code: {}", code);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the magic link email");

        } catch (Exception e) {
            log.error(" Failed to send magic link email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send magic link email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink, String code) {
        log.info(" Sending password reset email to: {} (User: {})", toEmail, userName);

        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("code", code);
            context.setVariable("expirationMinutes", 30);

            String htmlContent = templateEngine.process("password-reset-email", context);
            sendHtmlEmail(toEmail, "Password Reset Request ", htmlContent);

            log.info(" Password reset email sent successfully to: {}", toEmail);
            log.info(" Reset Link: {}", resetLink);
            log.info(" Reset Code: {}", code);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the reset email");

        } catch (Exception e) {
            log.error(" Failed to send password reset email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String userName, String verificationLink, String code) {
        log.info(" Sending email verification to: {} (User: {})", toEmail, userName);

        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("code", code);
            context.setVariable("expirationHours", 24);

            String htmlContent = templateEngine.process("email-verification", context);
            sendHtmlEmail(toEmail, "Verify Your Email Address ✉", htmlContent);

            log.info(" Verification email sent successfully to: {}", toEmail);
            log.info(" Verification Link: {}", verificationLink);
            log.info(" Verification Code: {}", code);
            log.info(" Check Mailpit UI at http://localhost:8025 to view the verification email");

        } catch (Exception e) {
            log.error(" Failed to send verification email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}