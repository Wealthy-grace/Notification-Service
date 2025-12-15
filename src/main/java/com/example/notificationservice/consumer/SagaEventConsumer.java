package com.example.notificationservice.consumer;

import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.domain.dto.AppointmentEventDto;
import com.example.notificationservice.domain.dto.BookingEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;

import java.math.BigDecimal;  // ← ADD THIS IMPORT

/**
 * 📧 SAGA Event Consumer for Notification Service
 * Listens to appointment and booking events and sends emails via Mailpit
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventConsumer {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    /**
     * 📥 Listen to APPOINTMENT events
     */
    @RabbitListener(queues = "${rabbitmq.queue.appointment:appointment-queue}", id = "appointmentSagaListener")
    @Transactional
    public void consumeAppointmentEvent(AppointmentEventDto event) {
        log.info("📥 [SAGA] Received appointment event: {} for appointment: {}",
                event.getEventType(), event.getAppointmentId());

        try {
            switch (event.getEventType()) {
                case "APPOINTMENT_CREATED":
                    sendAppointmentCreatedEmail(event);
                    break;

                case "APPOINTMENT_CONFIRMED":
                    sendAppointmentConfirmedEmail(event);
                    break;

                case "APPOINTMENT_CANCELLED":
                    sendAppointmentCancelledEmail(event);
                    break;

                case "APPOINTMENT_RESCHEDULED":
                    sendAppointmentRescheduledEmail(event);
                    break;

                default:
                    log.debug(" Appointment event type {} not handled", event.getEventType());
            }

        } catch (Exception e) {
            log.error(" Failed to process appointment event: {}", e.getMessage(), e);
        }
    }

    /**
     * 📥 Listen to BOOKING events
     */
    @RabbitListener(queues = "${rabbitmq.queue.booking:booking-queue}", id = "bookingSagaListener")
    @Transactional
    public void consumeBookingEvent(BookingEventDto event) {
        log.info("📥 [SAGA] Received booking event: {} for booking: {}",
                event.getEventType(), event.getBookingId());

        try {
            switch (event.getEventType()) {
                case "BOOKING_CREATED":
                    sendBookingCreatedEmail(event);
                    break;

                case "BOOKING_CONFIRMED":
                    sendBookingConfirmedEmail(event);
                    break;

                case "BOOKING_PAYMENT_COMPLETED":
                    sendBookingPaymentCompletedEmail(event);
                    break;

                case "BOOKING_CANCELLED":
                    sendBookingCancelledEmail(event);
                    break;

                case "BOOKING_EXPIRED":
                    sendBookingExpiredEmail(event);
                    break;

                default:
                    log.debug("📌 Booking event type {} not handled", event.getEventType());
            }

        } catch (Exception e) {
            log.error("❌ Failed to process booking event: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send APPOINTMENT_CREATED email
     */
    private void sendAppointmentCreatedEmail(AppointmentEventDto event) {
        log.info("📧 Sending APPOINTMENT_CREATED email to: {}", event.getRequesterEmail());

        try {
            Context context = new Context();
            context.setVariable("userName", event.getRequesterName());
            context.setVariable("appointmentDateTime", event.getAppointmentDateTime());
            context.setVariable("durationMinutes", event.getDurationMinutes());
            context.setVariable("propertyTitle", event.getPropertyTitle());
            context.setVariable("propertyAddress", event.getPropertyAddress());
            context.setVariable("providerName", event.getProviderName());
            context.setVariable("providerEmail", event.getProviderEmail());
            context.setVariable("providerPhone", event.getProviderPhone());
            context.setVariable("propertyUrl", "http://localhost:5173/property/" + event.getPropertyId());
            context.setVariable("appointmentUrl", "http://localhost:5173/appointments/" + event.getAppointmentId());

            String htmlContent = templateEngine.process("appointment-created-email", context);

            emailService.sendHtmlEmail(
                    event.getRequesterEmail(),
                    "📅 Appointment Created - " + event.getPropertyTitle(),
                    htmlContent
            );

            log.info("✅ APPOINTMENT_CREATED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send APPOINTMENT_CREATED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send APPOINTMENT_CONFIRMED email with beautiful HTML template
     * ✨ NOW USES THE NEW SERVICE METHOD WITH FULL STYLING!
     */
    private void sendAppointmentConfirmedEmail(AppointmentEventDto event) {
        log.info("📧 Sending APPOINTMENT_CONFIRMED email to: {}", event.getRequesterEmail());

        try {
            // Use the new EmailService method with all the beautiful HTML styling
            emailService.sendAppointmentConfirmedEmail(
                    event.getRequesterEmail(),
                    event.getRequesterName(),
                    event.getAppointmentDateTime(),
                    event.getDurationMinutes(),
                    event.getPropertyTitle(),
                    event.getPropertyAddress(),
                    event.getPropertyRentAmount(),
                    event.getPropertyImage(),
                    event.getProviderName(),
                    event.getProviderEmail(),
                    event.getProviderPhone(),
                    "http://localhost:5173/property/" + event.getPropertyId(),
                    "http://localhost:5173/appointments/" + event.getAppointmentId()
            );

            log.info("✅ APPOINTMENT_CONFIRMED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send APPOINTMENT_CONFIRMED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send APPOINTMENT_CANCELLED email
     */
    private void sendAppointmentCancelledEmail(AppointmentEventDto event) {
        log.info("📧 Sending APPOINTMENT_CANCELLED email to: {}", event.getRequesterEmail());

        try {
            String subject = "❌ Appointment Cancelled - " + event.getPropertyTitle();
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your appointment for %s has been cancelled.\n\n" +
                            "Reason: %s\n\n" +
                            "If you would like to reschedule, please create a new appointment.\n\n" +
                            "Best regards,\nStudent Housing Team",
                    event.getRequesterName(),
                    event.getPropertyTitle(),
                    event.getCancellationReason()
            );

            emailService.sendEmail(event.getRequesterEmail(), subject, body);

            log.info("✅ APPOINTMENT_CANCELLED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send APPOINTMENT_CANCELLED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send APPOINTMENT_RESCHEDULED email
     */
    private void sendAppointmentRescheduledEmail(AppointmentEventDto event) {
        log.info("📧 Sending APPOINTMENT_RESCHEDULED email to: {}", event.getRequesterEmail());

        try {
            String subject = "📅 Appointment Rescheduled - " + event.getPropertyTitle();
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your appointment for %s has been rescheduled.\n\n" +
                            "Previous Date: %s\n" +
                            "New Date: %s\n\n" +
                            "View appointment details: http://localhost:5173/appointments/%s\n\n" +
                            "Best regards,\nStudent Housing Team",
                    event.getRequesterName(),
                    event.getPropertyTitle(),
                    event.getPreviousDateTime(),
                    event.getAppointmentDateTime(),
                    event.getAppointmentId()
            );

            emailService.sendEmail(event.getRequesterEmail(), subject, body);

            log.info("✅ APPOINTMENT_RESCHEDULED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send APPOINTMENT_RESCHEDULED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send BOOKING_CREATED email
     */
    private void sendBookingCreatedEmail(BookingEventDto event) {
        log.info("📧 Sending BOOKING_CREATED email to: {}", event.getRequesterEmail());

        try {
            Context context = new Context();
            context.setVariable("userName", event.getRequesterName());
            context.setVariable("bookingId", event.getBookingId());
            context.setVariable("propertyTitle", event.getPropertyTitle());
            context.setVariable("propertyAddress", event.getPropertyAddress());
            context.setVariable("moveInDate", event.getMoveInDate());
            context.setVariable("moveOutDate", event.getMoveOutDate());
            context.setVariable("durationMonths", event.getBookingDurationMonths());
            context.setVariable("monthlyRent", event.getMonthlyRent());
            context.setVariable("totalRent", event.getMonthlyRent().multiply(java.math.BigDecimal.valueOf(event.getBookingDurationMonths())));
            context.setVariable("depositAmount", event.getDepositAmount());
            context.setVariable("totalAmount", event.getTotalAmount());
            context.setVariable("paymentDeadline", event.getPaymentDeadline());
            context.setVariable("providerName", event.getProviderName());
            context.setVariable("providerEmail", event.getProviderEmail());
            context.setVariable("providerPhone", event.getProviderPhone());
            context.setVariable("propertyImageUrl", event.getPropertyImage());
            context.setVariable("paymentUrl", "http://localhost:5173/bookings/" + event.getBookingId() + "/payment");
            context.setVariable("bookingUrl", "http://localhost:5173/bookings/" + event.getBookingId());

            String htmlContent = templateEngine.process("booking-created-email", context);

            emailService.sendHtmlEmail(
                    event.getRequesterEmail(),
                    "🎉 Booking Created - " + event.getPropertyTitle(),
                    htmlContent
            );

            log.info("✅ BOOKING_CREATED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send BOOKING_CREATED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send BOOKING_CONFIRMED email
     */
    private void sendBookingConfirmedEmail(BookingEventDto event) {
        log.info("📧 Sending BOOKING_CONFIRMED email to: {}", event.getRequesterEmail());

        try {
            String subject = "✅ Booking Confirmed - " + event.getPropertyTitle();
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Congratulations! Your booking has been confirmed.\n\n" +
                            "Booking ID: %s\n" +
                            "Property: %s\n" +
                            "Move-In Date: %s\n\n" +
                            "We'll send you the rental agreement soon.\n\n" +
                            "Best regards,\nStudent Housing Team",
                    event.getRequesterName(),
                    event.getBookingId(),
                    event.getPropertyTitle(),
                    event.getMoveInDate()
            );

            emailService.sendEmail(event.getRequesterEmail(), subject, body);

            log.info("✅ BOOKING_CONFIRMED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send BOOKING_CONFIRMED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send BOOKING_PAYMENT_COMPLETED email
     */
    private void sendBookingPaymentCompletedEmail(BookingEventDto event) {
        log.info("📧 Sending BOOKING_PAYMENT_COMPLETED email to: {}", event.getRequesterEmail());

        try {
            Context context = new Context();
            context.setVariable("userName", event.getRequesterName());
            context.setVariable("bookingId", event.getBookingId());
            context.setVariable("transactionId", event.getTransactionId());
            context.setVariable("paymentDate", event.getPaymentDate() != null ? event.getPaymentDate() : java.time.LocalDateTime.now());
            context.setVariable("paymentMethod", event.getPaymentMethod() != null ? event.getPaymentMethod() : "Credit Card");
            context.setVariable("propertyTitle", event.getPropertyTitle());
            context.setVariable("totalAmount", event.getTotalAmount());
            context.setVariable("moveInDate", event.getMoveInDate());
            context.setVariable("providerName", event.getProviderName());
            context.setVariable("providerEmail", event.getProviderEmail());
            context.setVariable("providerPhone", event.getProviderPhone());
            context.setVariable("receiptUrl", "http://localhost:5173/bookings/" + event.getBookingId() + "/receipt");
            context.setVariable("bookingUrl", "http://localhost:5173/bookings/" + event.getBookingId());

            String htmlContent = templateEngine.process("booking-payment-completed-email", context);

            emailService.sendHtmlEmail(
                    event.getRequesterEmail(),
                    "✅ Payment Confirmed - " + event.getPropertyTitle(),
                    htmlContent
            );

            log.info("✅ BOOKING_PAYMENT_COMPLETED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send BOOKING_PAYMENT_COMPLETED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send BOOKING_CANCELLED email
     */
    private void sendBookingCancelledEmail(BookingEventDto event) {
        log.info("📧 Sending BOOKING_CANCELLED email to: {}", event.getRequesterEmail());

        try {
            String subject = "❌ Booking Cancelled - " + event.getPropertyTitle();
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your booking has been cancelled.\n\n" +
                            "Booking ID: %s\n" +
                            "Property: %s\n" +
                            "Reason: %s\n\n" +
                            "If payment was made, a refund will be processed within 5-7 business days.\n\n" +
                            "Best regards,\nStudent Housing Team",
                    event.getRequesterName(),
                    event.getBookingId(),
                    event.getPropertyTitle(),
                    event.getCancellationReason()
            );

            emailService.sendEmail(event.getRequesterEmail(), subject, body);

            log.info("✅ BOOKING_CANCELLED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send BOOKING_CANCELLED email: {}", e.getMessage(), e);
        }
    }

    /**
     * 📧 Send BOOKING_EXPIRED email
     */
    private void sendBookingExpiredEmail(BookingEventDto event) {
        log.info("📧 Sending BOOKING_EXPIRED email to: {}", event.getRequesterEmail());

        try {
            String subject = "⚠️ Booking Expired - " + event.getPropertyTitle();
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your booking has expired due to non-payment.\n\n" +
                            "Booking ID: %s\n" +
                            "Property: %s\n\n" +
                            "If you're still interested in this property, please create a new booking.\n\n" +
                            "Best regards,\nStudent Housing Team",
                    event.getRequesterName(),
                    event.getBookingId(),
                    event.getPropertyTitle()
            );

            emailService.sendEmail(event.getRequesterEmail(), subject, body);

            log.info("✅ BOOKING_EXPIRED email sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send BOOKING_EXPIRED email: {}", e.getMessage(), e);
        }
    }
}