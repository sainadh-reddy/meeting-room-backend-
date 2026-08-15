package com.meetingroom.monolith.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class BookingStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingStatusScheduler.class);
    private final BookingRepository bookingRepository;

    public BookingStatusScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(fixedRate = 15000) // Runs every 15 seconds
    @Transactional
    public void updateMeetingStatusesDynamically() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Booking> bookings = bookingRepository.findAll();
        int updatedCount = 0;

        for (Booking booking : bookings) {
            if ("Cancelled".equalsIgnoreCase(booking.getStatus())) {
                continue;
            }

            LocalDate date = booking.getBookingDate();
            LocalTime start = booking.getStartTime();
            LocalTime end = booking.getEndTime();

            if (date == null || start == null || end == null) {
                continue;
            }

            String currentStatus = booking.getStatus();
            String targetStatus;

            if (date.isBefore(today)) {
                targetStatus = "Completed";
            } else if (date.isAfter(today)) {
                targetStatus = "Upcoming";
            } else {
                if (now.isBefore(start)) {
                    targetStatus = "Upcoming";
                } else if (!now.isBefore(start) && now.isBefore(end)) {
                    targetStatus = "Ongoing";
                } else {
                    targetStatus = "Completed";
                }
            }

            if (!targetStatus.equalsIgnoreCase(currentStatus)) {
                log.info("Dynamically updating booking #{} ({}) status from {} -> {}", 
                        booking.getId(), booking.getTitle(), currentStatus, targetStatus);
                booking.setStatus(targetStatus);
                bookingRepository.save(booking);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            log.info("Dynamic Meeting Status Scheduler updated {} meeting(s).", updatedCount);
        }
    }
}
