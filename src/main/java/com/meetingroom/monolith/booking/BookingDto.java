package com.meetingroom.monolith.booking;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingDto {

    public static class CreateRequest {
        private Long roomId;
        private String title;
        private String description;
        private LocalDate bookingDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer participants;

        // Recurrence parameters
        private String recurrenceType; // "NONE", "DAILY", "WEEKLY", "MONTHLY", "WEEKDAYS"
        private Integer recurrenceCount; // Number of occurrences (e.g. 5)
        private LocalDate endDate;        // Optional recurrence end date
        private Boolean includeSaturdays; // For weekend options

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDate getBookingDate() { return bookingDate; }
        public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public Integer getParticipants() { return participants; }
        public void setParticipants(Integer participants) { this.participants = participants; }

        public String getRecurrenceType() { return recurrenceType; }
        public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

        public Integer getRecurrenceCount() { return recurrenceCount; }
        public void setRecurrenceCount(Integer recurrenceCount) { this.recurrenceCount = recurrenceCount; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public Boolean getIncludeSaturdays() { return includeSaturdays; }
        public void setIncludeSaturdays(Boolean includeSaturdays) { this.includeSaturdays = includeSaturdays; }
    }
}
