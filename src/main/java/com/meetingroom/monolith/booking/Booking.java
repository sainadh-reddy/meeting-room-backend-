package com.meetingroom.monolith.booking;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    private String roomName;

    @Column(nullable = false)
    private Long userId;

    private String userEmail;
    private String userName;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private Integer participants;

    private String status; // "Upcoming", "Ongoing", "Completed", "Cancelled"

    private String recurrenceType; // "NONE", "DAILY", "WEEKLY", "MONTHLY", "WEEKDAYS"
    private Long parentBookingId; // ID of the first booking if part of a recurring series

    public Booking() {
    }

    public Booking(Long id, Long roomId, String roomName, Long userId, String userEmail, String userName,
                   String title, String description, LocalDate bookingDate, LocalTime startTime, LocalTime endTime,
                   Integer participants, String status, String recurrenceType, Long parentBookingId) {
        this.id = id;
        this.roomId = roomId;
        this.roomName = roomName;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.title = title;
        this.description = description;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participants = participants;
        this.status = status;
        this.recurrenceType = recurrenceType;
        this.parentBookingId = parentBookingId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    public Long getParentBookingId() { return parentBookingId; }
    public void setParentBookingId(Long parentBookingId) { this.parentBookingId = parentBookingId; }
}
