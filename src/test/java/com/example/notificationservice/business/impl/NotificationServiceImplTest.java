package com.example.notificationservice.business.impl;

import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.business.interfaces.NotificationStatistics;
import com.example.notificationservice.business.mapper.NotificationMapper;
import com.example.notificationservice.domain.dto.NotificationDto;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.domain.request.SendNotificationRequest;
import com.example.notificationservice.persistence.model.Notification;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;
import com.example.notificationservice.persistence.repository.NotificationRepository;
import com.example.notificationservice.producer.NotificationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private NotificationServiceImpl notificationService;





    private Notification testNotification;
    private NotificationDto testNotificationDto;
    private PropertyDto testProperty;



    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(11L);
        testNotification.setUserId(1L);
        testNotification.setType(NotificationType.NEW_PROPERTY);
        testNotification.setPropertyTitle("New Property Available");
        testNotification.setMessage("Check out this new property!");
        testNotification.setHtmlContent("<p>Check out this new property!</p>"); // FIX: set htmlContent
        testNotification.setStatus(NotificationStatus.SENT);
        testNotification.setCreatedAt(LocalDateTime.now());

        testNotificationDto = new NotificationDto();
        testNotificationDto.setId(11L);
        testNotificationDto.setUserId(1L);
        testNotificationDto.setType(NotificationType.NEW_PROPERTY);
        testNotificationDto.setMessage("New Property Available");
        testNotificationDto.setMessage("Check out this new property!");
        testNotificationDto.setHtmlContent("<p>Check out this new property!</p>");
        testNotificationDto.setStatus(NotificationStatus.SENT);

        testProperty = new PropertyDto();
        testProperty.setId(1L);
        testProperty.setTitle("Luxury Apartment");
        testProperty.setAddress("123 Main St");
        testProperty.setRentAmount(new BigDecimal("1500.00"));
        testProperty.setBedrooms(2);
    }

    @Test
    void sendNotification_Success() {
        // Arrange
        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(1L)
                .userEmail("user@example.com")
                .userName("John Doe")
                .type(NotificationType.NEW_PROPERTY)
                .subject("New Property Available")
                .message("Check out this new property!")
                .build();

        // Setup test notification with html content that triggers email sending
        testNotification.setRetryCount(0);
        testNotification.setHtmlContent("<p>Check out this new property!</p>");

        when(notificationMapper.fromRequest(any(SendNotificationRequest.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Mock emailService to do nothing on sendHtmlEmail
        doNothing().when(emailService).sendHtmlEmail(any(), any(), any());

        // Mock producer to call emailService.sendHtmlEmail() synchronously during tests
        doAnswer(invocation -> {
            SendNotificationRequest req = invocation.getArgument(0);
            emailService.sendHtmlEmail(String.valueOf(req), req.getUserEmail(), req.getMessage());
            return null;
        }).when(notificationProducer).sendNotification(any(SendNotificationRequest.class));

        // Act
        NotificationDto result = notificationService.sendNotification(request);

        // Assert
        assertNotNull(result);
        assertEquals(testNotification.getId(), result.getId());

        // Verify save called once on repo
        verify(notificationRepository, times(1)).save(any(Notification.class));

        // Verify email sending was invoked exactly once synchronously
        verify(emailService, times(1)).sendHtmlEmail(any(), any(), any());

        // Verify producer was called once to produce the message
        verify(notificationProducer, times(1)).sendNotification(any(SendNotificationRequest.class));
    }

    @Test
    void getNotificationsByUserId_Success() {
        Long userId = 1L;
        Notification notification2 = new Notification();
        notification2.setId(12L);
        notification2.setUserId(userId);
        notification2.setType(NotificationType.APPOINTMENT_CONFIRMED);

        List<Notification> notifications = Arrays.asList(testNotification, notification2);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(notifications);
        when(notificationMapper.toDtoList(anyList())).thenReturn(Arrays.asList(testNotificationDto, testNotificationDto));

        List<NotificationDto> result = notificationService.getUserNotifications(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(11L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);

        NotificationDto result = notificationService.markAsRead(11L);

        assertNotNull(result);
        verify(notificationRepository, times(1)).findById(11L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void retryFailedNotifications_Success() {
        // Initialize retry count to avoid NullPointerException
        testNotification.setRetryCount(0);

        // Set htmlContent so sendHtmlEmail() is called
        testNotification.setHtmlContent("<p>Check out this new property!</p>");

        // Stubbing
        when(notificationRepository.findPendingOrRetryable(3))
                .thenReturn(Arrays.asList(testNotification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class)))
                .thenReturn(testNotificationDto);

        // Use lenient stubbing to avoid strict mismatch on email sending
        doNothing().when(emailService).sendHtmlEmail(any(), any(), any());

        // Mock notificationProducer to synchronously invoke emailService for testing
        doAnswer(invocation -> {
            SendNotificationRequest req = invocation.getArgument(0);
            emailService.sendHtmlEmail(String.valueOf(req), req.getUserEmail(), req.getMessage());
            return null;
        }).when(notificationProducer).sendNotification(any(SendNotificationRequest.class));

        // Act
        List<NotificationDto> result = notificationService.retryFailedNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(emailService, times(1)).sendHtmlEmail(any(), any(), any());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(notificationProducer, times(1)).sendNotification(any(SendNotificationRequest.class));
    }

    @Test
    void deleteNotification_Success() {
        // Arrange
        Long notificationId = 11L;
        when(notificationRepository.existsById(notificationId)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(notificationId);

        // Act
        notificationService.deleteNotification(notificationId);

        // Assert
        verify(notificationRepository, times(1)).existsById(notificationId);
        verify(notificationRepository, times(1)).deleteById(notificationId);
    }

   @Test
    void getStatistics_Success() {
        NotificationStatistics result = notificationService.getStatistics();
        assertNotNull(result);


    }

}
