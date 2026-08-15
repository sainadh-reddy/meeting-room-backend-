package com.meetingroom.monolith.search;

import com.meetingroom.monolith.booking.Booking;
import com.meetingroom.monolith.booking.BookingRepository;
import com.meetingroom.monolith.room.Room;
import com.meetingroom.monolith.room.RoomRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public SearchController(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> searchAvailableRooms(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String block,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String vcFacility,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String facility,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {

        List<Room> activeRooms = roomRepository.findByStatus("Active");

        List<Room> filtered = activeRooms.stream().filter(room -> {
            if (location != null && !location.trim().isEmpty()) {
                if (room.getLocation() == null || !room.getLocation().equalsIgnoreCase(location.trim())) {
                    return false;
                }
            }
            if (campus != null && !campus.trim().isEmpty() && !"ALL".equalsIgnoreCase(campus)) {
                if (room.getCampus() == null || !room.getCampus().equalsIgnoreCase(campus.trim())) {
                    return false;
                }
            }
            if (block != null && !block.trim().isEmpty() && !"ALL".equalsIgnoreCase(block)) {
                if (room.getBlock() == null || !room.getBlock().equalsIgnoreCase(block.trim())) {
                    return false;
                }
            }
            if (floor != null && !floor.trim().isEmpty() && !"ALL".equalsIgnoreCase(floor)) {
                if (room.getFloor() == null || !room.getFloor().equalsIgnoreCase(floor.trim())) {
                    return false;
                }
            }
            if (vcFacility != null && !vcFacility.trim().isEmpty() && !"ALL".equalsIgnoreCase(vcFacility)) {
                if (room.getVcFacility() == null || !room.getVcFacility().equalsIgnoreCase(vcFacility.trim())) {
                    return false;
                }
            }
            if (zone != null && !zone.trim().isEmpty() && !"ALL".equalsIgnoreCase(zone)) {
                if (room.getZone() == null || !room.getZone().toLowerCase().contains(zone.trim().toLowerCase())) {
                    return false;
                }
            }
            if (minCapacity != null && room.getCapacity() != null && room.getCapacity() < minCapacity) {
                return false;
            }
            if (facility != null && !facility.trim().isEmpty()) {
                if (room.getFacilities() == null || !room.getFacilities().toLowerCase().contains(facility.trim().toLowerCase())) {
                    return false;
                }
            }
            if (date != null && startTime != null && endTime != null) {
                List<Booking> conflicts = bookingRepository.findConflictingBookings(room.getId(), date, startTime, endTime);
                if (!conflicts.isEmpty()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }
}
