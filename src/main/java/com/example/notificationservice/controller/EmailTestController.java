package com.example.notificationservice.controller;

import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.domain.dto.PropertyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    /**
     * Test endpoint - Send plain text email
     * GET http://localhost:8085/api/test/email/plain
     */
    @GetMapping("/plain")
    public ResponseEntity<Map<String, String>> sendPlainTextEmail(
            @RequestParam(defaultValue = "test@example.com") String email) {

        log.info("🧪 Testing plain text email to: {}", email);

        emailService.sendEmail(
                email,
                "Test Plain Text Email",
                "This is a test email from Notification Service.\n\n" +
                        "If you received this, your email service is working!\n\n" +
                        "Check Mailpit UI at: http://localhost:8025"
        );

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Plain text email sent successfully");
        response.put("recipient", email);
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send welcome email
     * GET http://localhost:8085/api/test/email/welcome
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, String>> sendWelcomeEmail(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 Testing welcome email to: {} (Name: {})", email, name);

        emailService.sendWelcomeEmail(email, name);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Welcome email sent successfully");
        response.put("recipient", email);
        response.put("userName", name);
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send magic link email
     * GET http://localhost:8085/api/test/email/magic-link
     */
    @GetMapping("/magic-link")
    public ResponseEntity<Map<String, String>> sendMagicLinkEmail(
            @RequestParam(defaultValue = "test@example.com") String email) {

        log.info("🧪 Testing magic link email to: {}", email);

        String magicLink = "http://localhost:5173/auth/verify?token=abc123xyz456";
        String code = "059429";

        emailService.sendMagicLinkEmail(email, magicLink, code);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Magic link email sent successfully");
        response.put("recipient", email);
        response.put("magicLink", magicLink);
        response.put("code", code);
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send password reset email
     * GET http://localhost:8085/api/test/email/password-reset
     */
    @GetMapping("/password-reset")
    public ResponseEntity<Map<String, String>> sendPasswordResetEmail(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 Testing password reset email to: {} (Name: {})", email, name);

        String resetLink = "http://localhost:5173/auth/reset-password?token=reset123";
        String code = "789456";

        emailService.sendPasswordResetEmail(email, name, resetLink, code);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Password reset email sent successfully");
        response.put("recipient", email);
        response.put("userName", name);
        response.put("resetLink", resetLink);
        response.put("code", code);
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send email verification
     * GET http://localhost:8085/api/test/email/verify
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> sendVerificationEmail(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 Testing email verification to: {} (Name: {})", email, name);

        String verificationLink = "http://localhost:5173/auth/verify-email?token=verify123";
        String code = "VERIFY789";

        emailService.sendVerificationEmail(email, name, verificationLink, code);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Verification email sent successfully");
        response.put("recipient", email);
        response.put("userName", name);
        response.put("verificationLink", verificationLink);
        response.put("code", code);
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send new property notification
     * GET http://localhost:8085/api/test/email/property
     */
    @GetMapping("/property")
    public ResponseEntity<Map<String, String>> sendPropertyNotification(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 Testing property notification email to: {} (Name: {})", email, name);

        // Create test property
        PropertyDto property = new PropertyDto();
        property.setId(1L);
        property.setTitle("Luxurious 2-Bedroom Apartment in City Center");
        property.setAddress("123 Main Street, Utrecht, Netherlands");
        property.setRentAmount(BigDecimal.valueOf(1500.0));
        property.setBedrooms(2);
        property.setSurfaceArea(String.valueOf(85.0));
        property.setImages(List.of("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600"));

        emailService.sendNewPropertyNotification(email, name, property);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Property notification email sent successfully");
        response.put("recipient", email);
        response.put("userName", name);
        response.put("propertyTitle", property.getTitle());
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send appointment confirmation
     * GET http://localhost:8085/api/test/email/appointment
     */
    @GetMapping("/appointment")
    public ResponseEntity<Map<String, String>> sendAppointmentConfirmation(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 Testing appointment confirmation email to: {} (Name: {})", email, name);

        String appointmentDetails = "Property Viewing\n" +
                "Property: Luxurious 2-Bedroom Apartment\n" +
                "Address: 123 Main Street, Utrecht\n" +
                "Date: March 20, 2025\n" +
                "Time: 2:00 PM\n" +
                "Duration: 30 minutes";

        emailService.sendAppointmentConfirmation(email, name, appointmentDetails);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Appointment confirmation email sent successfully");
        response.put("recipient", email);
        response.put("userName", name);
        response.put("appointmentDetails", appointmentDetails);
        response.put("mailpit_ui", "http://localhost:8025");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint - Send all email types
     * GET http://localhost:8085/api/test/email/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> sendAllEmails(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 Testing ALL email types to: {} (Name: {})", email, name);

        try {
            // 1. Plain text
            emailService.sendEmail(email, "Test: Plain Text", "This is a plain text email test.");

            // 2. Welcome
            emailService.sendWelcomeEmail(email, name);

            // 3. Magic Link
            emailService.sendMagicLinkEmail(email, "http://localhost:5173/auth/verify?token=test", "123456");

            // 4. Password Reset
            emailService.sendPasswordResetEmail(email, name, "http://localhost:5173/auth/reset?token=test", "789456");

            // 5. Email Verification
            emailService.sendVerificationEmail(email, name, "http://localhost:5173/auth/verify-email?token=test", "VER123");

            // 6. Property Notification
            PropertyDto property = new PropertyDto();
            property.setId(1L);
            property.setTitle("Test Property");
            property.setAddress("123 Test St");
            property.setRentAmount(BigDecimal.valueOf(1200.0));
            property.setBedrooms(2);
            property.setSurfaceArea(String.valueOf(80.0));
            emailService.sendNewPropertyNotification(email, name, property);

            // 7. Appointment Confirmation
            emailService.sendAppointmentConfirmation(email, name, "Test Appointment on Jan 1, 2025");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All email types sent successfully");
            response.put("recipient", email);
            response.put("userName", name);
            response.put("emailsSent", 7);
            response.put("emailTypes", List.of(
                    "Plain Text", "Welcome", "Magic Link", "Password Reset",
                    "Email Verification", "Property Notification", "Appointment Confirmation"
            ));
            response.put("mailpit_ui", "http://localhost:8025");
            response.put("note", "Check Mailpit UI to see all 7 test emails");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error sending test emails: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send all emails");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get Mailpit info
     * GET http://localhost:8085/api/test/email/mailpit-info
     */
    @GetMapping("/mailpit-info")
    public ResponseEntity<Map<String, String>> getMailpitInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("mailpit_web_ui", "http://localhost:8025");
        info.put("smtp_host", "localhost");
        info.put("smtp_port", "1025");
        info.put("status", "Check the Web UI to verify Mailpit is running");
        info.put("documentation", "See MAILPIT_SETUP_GUIDE.md for details");

        return ResponseEntity.ok(info);
    }
}