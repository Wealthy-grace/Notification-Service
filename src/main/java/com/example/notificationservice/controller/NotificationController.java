package com.example.notificationservice.controller;

import com.example.notificationservice.business.client.PropertyServiceClient;
import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.business.interfaces.NotificationService;
import com.example.notificationservice.business.interfaces.NotificationStatistics;
import com.example.notificationservice.domain.dto.NotificationDto;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.domain.request.NotifyNewPropertyRequest;
import com.example.notificationservice.domain.request.PropertyRequest;
import com.example.notificationservice.domain.request.SendNotificationRequest;
import com.example.notificationservice.domain.request.UpdatePropertyRentedRequest;
import com.example.notificationservice.domain.response.NotificationListResponse;
import com.example.notificationservice.domain.response.NotificationResponse;
import com.example.notificationservice.domain.response.PropertyServiceResponse;
import com.example.notificationservice.exceptions.ResourceNotFoundException;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing email notifications")
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {

    private final NotificationService notificationService;
    private final PropertyServiceClient propertyServiceClient;
    private final EmailService emailService;

    /**
     * Send a single notification
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    @Operation(summary = "Send notification", description = "Send a single notification to a user")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("REST request to send notification to: {}", request.getUserEmail());

        //  If propertyId exists, fetch property details and populate images
        if (request.getPropertyId() != null) {
            try {
                log.info("Fetching property details for ID: {}", request.getPropertyId());
                PropertyServiceResponse propertyResponse = propertyServiceClient
                        .getPropertyById(request.getPropertyId());

                if (propertyResponse != null && propertyResponse.getPropertyId() != null) {
                    // Populate image URLs from property response
                    request.setPropertyImageUrl(propertyResponse.getImage());
                    request.setPropertyImageUrl2(propertyResponse.getImage2());
                    request.setPropertyImageUrl3(propertyResponse.getImage3());

                    log.info(" Property images populated successfully");
                }
            } catch (Exception e) {
                log.warn("Could not fetch property details for ID: {}. Error: {}",
                        request.getPropertyId(), e.getMessage());
            }
        }

        NotificationDto notification = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationResponse.success("Notification sent successfully", notification));
    }

    /**
     * Notify about new property
     */
    @PostMapping("/new-property")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    @Operation(summary = "Notify new property", description = "Send notifications about new property to multiple users")
    public ResponseEntity<NotificationListResponse> notifyNewProperty(
            @Valid @RequestBody NotifyNewPropertyRequest request) {
        log.info("REST request to notify about new property: {}", request.getPropertyId());

        List<NotificationDto> notifications = notificationService.notifyNewProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationListResponse.success("Property notifications sent successfully", notifications));
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        log.info("REST request to get notification: {}", id);

        NotificationDto notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(NotificationResponse.success("Notification retrieved successfully", notification));
    }

    /**
     * Get user's notifications
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user notifications", description = "Get all notifications for a specific user")
    public ResponseEntity<NotificationListResponse> getUserNotifications(@PathVariable Long userId) {
        log.info("REST request to get notifications for user: {}", userId);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(NotificationListResponse.success("User notifications retrieved successfully", notifications));
    }

    /**
     * Get notifications by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notifications by status", description = "Admin only")
    public ResponseEntity<NotificationListResponse> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        log.info("REST request to get notifications with status: {}", status);

        List<NotificationDto> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(NotificationListResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notifications by type", description = "Admin only")
    public ResponseEntity<NotificationListResponse> getNotificationsByType(@PathVariable NotificationType type) {
        log.info("REST request to get notifications of type: {}", type);

        List<NotificationDto> notifications = notificationService.getNotificationsByType(type);
        return ResponseEntity.ok(NotificationListResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * Get all notifications
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all notifications", description = "Admin only")
    public ResponseEntity<NotificationListResponse> getAllNotifications() {
        log.info("REST request to get all notifications");

        List<NotificationDto> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(NotificationListResponse.success("All notifications retrieved successfully", notifications));
    }

    /**
     * Retry failed notifications
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retry failed notifications", description = "Retry sending failed notifications - Admin only")
    public ResponseEntity<NotificationListResponse> retryFailedNotifications() {
        log.info("REST request to retry failed notifications");

        List<NotificationDto> notifications = notificationService.retryFailedNotifications();
        return ResponseEntity.ok(NotificationListResponse.success("Failed notifications retried", notifications));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("REST request to mark notification as read: {}", id);

        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(NotificationResponse.success("Notification marked as read", notification));
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete notification", description = "Admin only")
    public ResponseEntity<NotificationResponse> deleteNotification(@PathVariable Long id) {
        log.info("REST request to delete notification: {}", id);

        notificationService.deleteNotification(id);
        return ResponseEntity.ok(NotificationResponse.success("Notification deleted successfully", null));
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification statistics", description = "Admin only")
    public ResponseEntity<NotificationStatistics> getStatistics() {
        log.info("REST request to get notification statistics");

        NotificationStatistics stats = notificationService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    // ====================================================================
    //  MAILPIT TESTING ENDPOINTS (Development Only)
    // These endpoints bypass authentication for easy testing
    // Remove or secure these in production!
    // ====================================================================

    /**
     *  Test: Send welcome email (No Auth Required)
     * GET /api/notifications/test/welcome?email=test@example.com&name=John
     */
    @GetMapping("/test/welcome")
    @Operation(summary = "TEST: Send welcome email", description = "Testing endpoint - No auth required")
    public ResponseEntity<Map<String, String>> testWelcomeEmail(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info(" TEST: Sending welcome email to: {} (Name: {})", email, name);

        try {
            emailService.sendWelcomeEmail(email, name);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Welcome email sent successfully");
            response.put("recipient", email);
            response.put("userName", name);
            response.put("mailpit_ui", "http://localhost:8025");
            response.put("note", "Check Mailpit UI to view the email");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send welcome email");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

//
//   Test: Send magic link email (No Auth Required)
//      GET /api/notifications/test/magic-link?email=test@example.com
//
    @GetMapping("/test/magic-link")
    @Operation(summary = "TEST: Send magic link email", description = "Testing endpoint - No auth required")
    public ResponseEntity<Map<String, String>> testMagicLinkEmail(
            @RequestParam(defaultValue = "test@example.com") String email) {

        log.info(" TEST: Sending magic link email to: {}", email);

        try {
            String magicLink = "http://localhost:5173/auth/verify?token=test-token-" + System.currentTimeMillis();
            String code = String.format("%06d", (int) (Math.random() * 1000000));

            emailService.sendMagicLinkEmail(email, magicLink, code);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Magic link email sent successfully");
            response.put("recipient", email);
            response.put("magicLink", magicLink);
            response.put("code", code);
            response.put("mailpit_ui", "http://localhost:8025");
            response.put("note", "Check Mailpit UI to view the email");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" Failed to send magic link email: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send magic link email");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 🧪 Test: Send property notification (No Auth Required)
     * GET /api/notifications/test/property?email=test@example.com&name=John
     */
    @GetMapping("/test/property")
    @Operation(summary = "TEST: Send property notification", description = "Testing endpoint - No auth required")
    public ResponseEntity<Map<String, String>> testPropertyNotification(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name) {

        log.info("🧪 TEST: Sending property notification to: {} (Name: {})", email, name);

        try {
            // Create test property
            PropertyDto property = new PropertyDto();
            property.setId(999L);
            property.setTitle("Beautiful 2-Bedroom Apartment - TEST");
            property.setAddress("123 Test Street, Utrecht, Netherlands");
            property.setRentAmount(BigDecimal.valueOf(1350.0));
            property.setBedrooms(2);
            property.setSurfaceArea(String.valueOf(85.0));
            property.setImages(List.of("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600"));

            emailService.sendNewPropertyNotification(email, name, property);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Property notification sent successfully");
            response.put("recipient", email);
            response.put("userName", name);
            response.put("propertyTitle", property.getTitle());
            response.put("mailpit_ui", "http://localhost:8025");
            response.put("note", "Check Mailpit UI to view the email");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" Failed to send property notification: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send property notification");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 🧪 Test: Get Mailpit info (No Auth Required)
     * GET /api/notifications/test/mailpit-info
     */
    @GetMapping("/test/mailpit-info")
    @Operation(summary = "TEST: Get Mailpit info", description = "Get Mailpit configuration info")
    public ResponseEntity<Map<String, String>> getMailpitInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("mailpit_web_ui", "http://localhost:8025");
        info.put("smtp_host", "localhost");
        info.put("smtp_port", "1025");
        info.put("status", "Check the Web UI to verify Mailpit is running");
        info.put("documentation", "See MAILPIT_SETUP_GUIDE.md for details");
        info.put("test_endpoints", "Use /api/notifications/test/* for testing");

        return ResponseEntity.ok(info);
    }

    @PutMapping("/property/{propertyId}/rented")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    @Operation(summary = "Update property rental status",
            description = "Updates whether a property is currently rented")
    public ResponseEntity<PropertyServiceResponse> updatePropertyIsRented(
            @PathVariable Long propertyId,
            @Valid @RequestBody UpdatePropertyRentedRequest request) {

        log.info(" Received request to update rental status for property ID: {} to {}",
                propertyId, request.getPropertyIsRented());

        try {
            PropertyServiceResponse response = notificationService.updatePropertyIsRented(
                    propertyId,
                    request.getPropertyIsRented()
            );

            log.info(" Successfully updated rental status for property ID: {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error(" Property not found with ID: {}", propertyId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error(" Error processing request for property ID: {}", propertyId, e);
            return ResponseEntity.internalServerError().build();
        }
    }



//      Safely extract image URL from PropertyDto images list
//
    private String getPropertyImageUrl(PropertyDto property, int index) {
        if (property.getImages() != null &&
                !property.getImages().isEmpty() &&
                property.getImages().size() > index) {
            return property.getImages().get(index);
        }
        return null;
    }
}