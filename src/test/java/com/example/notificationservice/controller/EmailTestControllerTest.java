package com.example.notificationservice.controller;

import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.persistence.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = EmailTestController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        }
)
public class EmailTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendPlainTextEmail_Success() throws Exception {
        String testEmail = "test@example.com";
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        mockMvc.perform(get("/api/test/email/plain")
                        .param("email", testEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Plain text email sent successfully"))
                .andExpect(jsonPath("$.recipient").value(testEmail))
                .andExpect(jsonPath("$.mailpit_ui").value("http://localhost:8025"));

        verify(emailService, times(1)).sendEmail(eq(testEmail), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void sendWelcomeEmail_Success() throws Exception {
        String testEmail = "newuser@example.com";
        String testName = "John Doe";
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        mockMvc.perform(get("/api/test/email/welcome")
                        .param("email", testEmail)
                        .param("name", testName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Welcome email sent successfully"))
                .andExpect(jsonPath("$.recipient").value(testEmail))
                .andExpect(jsonPath("$.userName").value(testName))
                .andExpect(jsonPath("$.mailpit_ui").value("http://localhost:8025"));

        verify(emailService, times(1)).sendWelcomeEmail(eq(testEmail), eq(testName));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendPropertyNotification_Success() throws Exception {
        String testEmail = "student@example.com";
        String testName = "Alice Student";
        doNothing().when(emailService).sendNewPropertyNotification(anyString(), anyString(), any(PropertyDto.class));

        mockMvc.perform(get("/api/test/email/property")
                        .param("email", testEmail)
                        .param("name", testName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Property notification email sent successfully"))
                .andExpect(jsonPath("$.recipient").value(testEmail))
                .andExpect(jsonPath("$.userName").value(testName))
                .andExpect(jsonPath("$.propertyTitle").value("Luxurious 2-Bedroom Apartment in City Center"))
                .andExpect(jsonPath("$.mailpit_ui").value("http://localhost:8025"));

        verify(emailService, times(1))
                .sendNewPropertyNotification(eq(testEmail), eq(testName), any(PropertyDto.class));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void sendMagicLinkEmail_Success() throws Exception {
        String testEmail = "user@example.com";
        doNothing().when(emailService).sendMagicLinkEmail(anyString(), anyString(), anyString());

        mockMvc.perform(get("/api/test/email/magic-link")
                        .param("email", testEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Magic link email sent successfully"))
                .andExpect(jsonPath("$.recipient").value(testEmail))
                .andExpect(jsonPath("$.magicLink").exists())
                .andExpect(jsonPath("$.code").value("059429"))
                .andExpect(jsonPath("$.mailpit_ui").value("http://localhost:8025"));

        verify(emailService, times(1)).sendMagicLinkEmail(eq(testEmail), anyString(), eq("059429"));
    }
}