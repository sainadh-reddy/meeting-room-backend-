package com.meetingroom.monolith.booking;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(@RequestHeader("X-Auth-UserId") Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Booking>> getBookingsByRoomAndDate(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getBookingsByRoomAndDate(roomId, date));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingDto.CreateRequest request,
                                           @RequestHeader("X-Auth-UserId") Long userId) {
        try {
            List<Booking> bookings = bookingService.createBookings(request, userId);
            return ResponseEntity.ok(bookings);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           @RequestHeader("X-Auth-UserId") Long userId,
                                           @RequestHeader(value = "X-Auth-Role", defaultValue = "EMPLOYEE") String role) {
        try {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            Booking cancelled = bookingService.cancelBooking(id, userId, isAdmin);
            return ResponseEntity.ok(cancelled);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
