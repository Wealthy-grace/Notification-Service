package com.example.notificationservice.controller;

import com.example.notificationservice.business.client.PropertyServiceClient;
import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.business.interfaces.NotificationService;
import com.example.notificationservice.domain.dto.NotificationDto;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;
import com.example.notificationservice.persistence.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private PropertyServiceClient propertyServiceClient;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private NotificationRepository notificationRepository;

    private NotificationDto testNotification;

    @BeforeEach
    void setUp() {
        testNotification = NotificationDto.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.APPOINTMENT_CONFIRMED)
                .htmlContent("Check out this new property!")
                .message("Check out this new property!")
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNotificationsByUserId_Success() throws Exception {
        Long userId = 1L;
        NotificationDto notification2 = NotificationDto.builder()
                .id(2L)
                .userId(userId)
                .type(NotificationType.NEW_PROPERTY)
                .htmlContent("Your appointment is confirmed")
                .message("Your appointment is confirmed")
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        List<NotificationDto> notifications = Arrays.asList(testNotification, notification2);
        when(notificationService.getUserNotifications(userId)).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications.length()").value(2))
                .andExpect(jsonPath("$.notifications[0].id").value(1))
                .andExpect(jsonPath("$.notifications[1].id").value(2));

        verify(notificationService, times(1)).getUserNotifications(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNotificationById_Success() throws Exception {
        Long notificationId = 1L;
        when(notificationService.getNotificationById(notificationId)).thenReturn(testNotification);

        mockMvc.perform(get("/api/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.notification.id").value(1))
                .andExpect(jsonPath("$.notification.type").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$.notification.status").value("PENDING"));

        verify(notificationService, times(1)).getNotificationById(notificationId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markNotificationAsRead_Success() throws Exception {
        Long notificationId = 1L;
        NotificationDto readNotification = NotificationDto.builder()
                .id(notificationId)
                .userId(1L)
                .type(NotificationType.NEW_PROPERTY)
                .htmlContent("Check out this new property!")
                .message("Check out this new property!")
                .status(NotificationStatus.SENT)
                .build();

        when(notificationService.markAsRead(notificationId)).thenReturn(readNotification);

        mockMvc.perform(put("/api/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification marked as read"))
                .andExpect(jsonPath("$.notification.status").value("SENT"));

        verify(notificationService, times(1)).markAsRead(notificationId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotification_Success() throws Exception {
        Long notificationId = 1L;
        doNothing().when(notificationService).deleteNotification(notificationId);

        mockMvc.perform(delete("/api/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));

        verify(notificationService, times(1)).deleteNotification(notificationId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNotificationsByUserId_NoNotificationsFound() throws Exception {
        Long userId = 99L;
        when(notificationService.getUserNotifications(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications.length()").value(0))
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.message").value("User notifications retrieved successfully"));

        verify(notificationService, times(1)).getUserNotifications(userId);
    }


}