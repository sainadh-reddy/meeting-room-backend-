package com.meetingroom.monolith.notification;

import com.meetingroom.monolith.booking.Booking;
import com.meetingroom.monolith.booking.BookingEvent;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final JavaMailSender mailSender;

    public NotificationListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @EventListener
    public void handleBookingEvent(BookingEvent event) {
        Booking booking = event.getBooking();
        String type = event.getEventType();

        System.out.println("[NOTIFICATION EVENT] Processing " + type + " notification for booking ID: " + booking.getId() +
                " (User: " + booking.getUserEmail() + ", Room: " + booking.getRoomName() + ")");

        if (booking.getUserEmail() == null || booking.getUserEmail().isEmpty()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(booking.getUserEmail());
            helper.setFrom("notifications@meetingroom.com");

            if ("CREATED".equalsIgnoreCase(type)) {
                helper.setSubject("Meeting Room Booking Confirmation: " + booking.getTitle());
                String htmlContent = "<h2>Booking Confirmation</h2>" +
                        "<p>Hi <b>" + (booking.getUserName() != null ? booking.getUserName() : "User") + "</b>,</p>" +
                        "<p>Your booking for <b>" + booking.getRoomName() + "</b> has been confirmed.</p>" +
                        "<ul>" +
                        "<li><b>Meeting Title:</b> " + booking.getTitle() + "</li>" +
                        "<li><b>Date:</b> " + booking.getBookingDate() + "</li>" +
                        "<li><b>Time:</b> " + booking.getStartTime() + " - " + booking.getEndTime() + "</li>" +
                        "</ul>" +
                        "<p>Thank you for using the Meeting Room Booking System!</p>";
                helper.setText(htmlContent, true);
            } else if ("CANCELLED".equalsIgnoreCase(type)) {
                helper.setSubject("Meeting Room Booking Cancellation: " + booking.getTitle());
                String htmlContent = "<h2>Booking Cancelled</h2>" +
                        "<p>Hi <b>" + (booking.getUserName() != null ? booking.getUserName() : "User") + "</b>,</p>" +
                        "<p>Your booking for <b>" + booking.getRoomName() + "</b> on " + booking.getBookingDate() +
                        " has been cancelled.</p>";
                helper.setText(htmlContent, true);
            }

            mailSender.send(message);
            System.out.println("[NOTIFICATION SENT] Email successfully dispatched to " + booking.getUserEmail());
        } catch (Exception e) {
            System.err.println("[NOTIFICATION WARN] Failed to send email via SMTP (MailHog may be offline): " + e.getMessage());
        }
    }
}
