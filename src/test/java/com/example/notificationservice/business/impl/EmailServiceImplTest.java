package com.example.notificationservice.business.impl;

import com.example.notificationservice.domain.dto.PropertyDto;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // Set up @Value properties using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@example.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Property Rental Platform");
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);

        // Create a mock MimeMessage
        Session session = Session.getInstance(new Properties());
        mimeMessage = new MimeMessage(session);
    }

    @Test
    void sendEmail_Success() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        emailService.sendEmail(to, subject, body);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendWelcomeEmail_Success() {
        // Arrange
        String toEmail = "newuser@example.com";
        String userName = "John Doe";
        String htmlContent = "<html><body>Welcome!</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendWelcomeEmail(toEmail, userName);

        // Assert
        verify(templateEngine, times(1)).process(eq("welcome-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendNewPropertyNotification_Success() {
        // Arrange
        String toEmail = "student@example.com";
        String userName = "Jane Student";

        PropertyDto property = new PropertyDto();
        property.setId(1L);
        property.setTitle("Luxury Apartment");
        property.setAddress("123 Main St");
        property.setRentAmount(new BigDecimal("1500.00"));
        property.setBedrooms(2);
        property.setSurfaceArea("85");
        property.setImages(List.of("http://example.com/image.jpg"));

        String htmlContent = "<html><body>New Property Available!</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("new-property-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendNewPropertyNotification(toEmail, userName, property);

        // Assert
        verify(templateEngine, times(1)).process(eq("new-property-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendMagicLinkEmail_Success() {
        // Arrange
        String toEmail = "user@example.com";
        String magicLink = "http://localhost:5173/auth/verify?token=abc123";
        String code = "123456";
        String htmlContent = "<html><body>Magic Link</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("magic-link-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendMagicLinkEmail(toEmail, magicLink, code);

        // Assert
        verify(templateEngine, times(1)).process(eq("magic-link-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Arrange
        String toEmail = "user@example.com";
        String userName = "Test User";
        String resetLink = "http://localhost:5173/auth/reset?token=xyz789";
        String code = "789456";
        String htmlContent = "<html><body>Password Reset</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendPasswordResetEmail(toEmail, userName, resetLink, code);

        // Assert
        verify(templateEngine, times(1)).process(eq("password-reset-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Failure() {
        // Arrange
        String toEmail = "user@example.com";
        String userName = "Test User";
        String resetLink = "http://localhost:5173/auth/reset?token=xyz789";
        String code = "789456";
        String htmlContent = "<html><body>Password Reset</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendPasswordResetEmail(toEmail, userName, resetLink, code);

        // Assert
        verify(templateEngine, times(1)).process(eq("password-reset-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmail_Failure() {
        // Arrange
        String toEmail = "user@example.com";
        String userName = "Test User";
        String htmlContent = "<html><body>Welcome!</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendWelcomeEmail(toEmail, userName);

        // Assert
        verify(templateEngine, times(1)).process(eq("welcome-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmails_Success() {
        // Arrange
        String toEmail = "user@example.com";
        String userName = "Test User";
        String htmlContent = "<html><body>Welcome!</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-email"), any(Context.class)))
                .thenReturn(htmlContent);

        // Act
        emailService.sendWelcomeEmail(toEmail, userName);

        // Assert
        verify(templateEngine, times(1)).process(eq("welcome-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

}