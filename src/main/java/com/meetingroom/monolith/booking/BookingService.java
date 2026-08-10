package com.meetingroom.monolith.booking;

import com.meetingroom.monolith.auth.User;
import com.meetingroom.monolith.auth.UserRepository;
import com.meetingroom.monolith.room.Room;
import com.meetingroom.monolith.room.RoomRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public Booking updateDynamicStatus(Booking booking) {
        if (booking == null || "Cancelled".equalsIgnoreCase(booking.getStatus())) {
            return booking;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalDate date = booking.getBookingDate();
        LocalTime start = booking.getStartTime();
        LocalTime end = booking.getEndTime();

        if (date == null || start == null || end == null) {
            return booking;
        }

        String newStatus;

        if (date.isBefore(today)) {
            newStatus = "Completed";
        } else if (date.isAfter(today)) {
            newStatus = "Upcoming";
        } else {
            if (now.isBefore(start)) {
                newStatus = "Upcoming";
            } else if (!now.isBefore(start) && now.isBefore(end)) {
                newStatus = "Ongoing";
            } else {
                newStatus = "Completed";
            }
        }

        if (!newStatus.equalsIgnoreCase(booking.getStatus())) {
            booking.setStatus(newStatus);
            return bookingRepository.save(booking);
        }

        return booking;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        for (Booking b : bookings) {
            updateDynamicStatus(b);
        }
        return bookings;
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingDateDescStartTimeAsc(userId);
        for (Booking b : bookings) {
            updateDynamicStatus(b);
        }
        return bookings;
    }

    public Optional<Booking> getBookingById(Long id) {
        Optional<Booking> opt = bookingRepository.findById(id);
        opt.ifPresent(this::updateDynamicStatus);
        return opt;
    }

    public List<Booking> getBookingsByRoomAndDate(Long roomId, LocalDate date) {
        List<Booking> bookings = bookingRepository.findByRoomIdAndBookingDate(roomId, date);
        for (Booking b : bookings) {
            updateDynamicStatus(b);
        }
        return bookings;
    }

    @Transactional
    public List<Booking> createBookings(BookingDto.CreateRequest request, Long userId) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + request.getRoomId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<LocalDate> bookingDates = calculateRecurrenceDates(request);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (LocalDate date : bookingDates) {
            if (date.isBefore(today)) {
                throw new IllegalArgumentException("Cannot book a meeting room for a past date: " + date);
            }
            if (date.isEqual(today) && request.getStartTime().isBefore(now)) {
                throw new IllegalArgumentException("Cannot book a meeting room for a past time slot (" + request.getStartTime() + ")");
            }

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    request.getRoomId(), date, request.getStartTime(), request.getEndTime());
            if (!conflicts.isEmpty()) {
                throw new IllegalStateException("Slot conflict detected on date: " + date + " between " +
                        request.getStartTime() + " and " + request.getEndTime());
            }
        }

        List<Booking> createdBookings = new ArrayList<>();
        Long parentId = null;

        for (int i = 0; i < bookingDates.size(); i++) {
            LocalDate date = bookingDates.get(i);
            Booking booking = new Booking();
            booking.setRoomId(room.getId());
            booking.setRoomName(room.getName());
            booking.setUserId(user.getId());
            booking.setUserEmail(user.getEmail());
            booking.setUserName(user.getName());
            booking.setTitle(request.getTitle());
            booking.setDescription(request.getDescription());
            booking.setBookingDate(date);
            booking.setStartTime(request.getStartTime());
            booking.setEndTime(request.getEndTime());
            booking.setParticipants(request.getParticipants());

            // Determine initial status based on creation time & date
            String initialStatus = "Upcoming";
            if (date.isEqual(today)) {
                if (!now.isBefore(request.getStartTime()) && now.isBefore(request.getEndTime())) {
                    initialStatus = "Ongoing";
                } else if (!now.isBefore(request.getEndTime())) {
                    initialStatus = "Completed";
                }
            }
            booking.setStatus(initialStatus);
            booking.setRecurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : "NONE");

            if (i > 0 && parentId != null) {
                booking.setParentBookingId(parentId);
            }

            Booking saved = bookingRepository.save(booking);

            if (i == 0) {
                parentId = saved.getId();
                saved.setParentBookingId(parentId);
                saved = bookingRepository.save(saved);
            }

            createdBookings.add(saved);

            eventPublisher.publishEvent(new BookingEvent(this, saved, "CREATED"));
        }

        return createdBookings;
    }

    private List<LocalDate> calculateRecurrenceDates(BookingDto.CreateRequest req) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = req.getBookingDate();
        String recType = req.getRecurrenceType() != null ? req.getRecurrenceType().toUpperCase() : "NONE";

        if ("NONE".equals(recType)) {
            dates.add(current);
            return dates;
        }

        int maxCount = req.getRecurrenceCount() != null && req.getRecurrenceCount() > 0 ? req.getRecurrenceCount() : 1;
        LocalDate cutoff = req.getEndDate() != null ? req.getEndDate() : current.plusMonths(3);
        boolean includeSaturdays = Boolean.TRUE.equals(req.getIncludeSaturdays());

        while (dates.size() < maxCount && !current.isAfter(cutoff)) {
            DayOfWeek dow = current.getDayOfWeek();
            boolean isSunday = dow == DayOfWeek.SUNDAY;
            boolean isSaturday = dow == DayOfWeek.SATURDAY;

            if ("WEEKDAYS".equals(recType)) {
                if (!isSunday && (!isSaturday || includeSaturdays)) {
                    dates.add(current);
                }
                current = current.plusDays(1);
            } else if ("DAILY".equals(recType)) {
                if (!isSunday && (!isSaturday || includeSaturdays)) {
                    dates.add(current);
                }
                current = current.plusDays(1);
            } else if ("WEEKLY".equals(recType)) {
                dates.add(current);
                current = current.plusWeeks(1);
            } else if ("MONTHLY".equals(recType)) {
                dates.add(current);
                current = current.plusMonths(1);
            } else {
                dates.add(current);
                break;
            }
        }

        return dates;
    }

    @Transactional
    public Booking cancelBooking(Long id, Long requestingUserId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        if (!isAdmin && !booking.getUserId().equals(requestingUserId)) {
            throw new SecurityException("Unauthorized to cancel this booking");
        }

        booking.setStatus("Cancelled");
        Booking updated = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingEvent(this, updated, "CANCELLED"));

        return updated;
    }
}
