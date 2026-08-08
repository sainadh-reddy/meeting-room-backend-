package com.meetingroom.monolith.room;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Room>> getActiveRooms() {
        return ResponseEntity.ok(roomService.getActiveRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.saveRoom(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        return roomService.getRoomById(id).map(room -> {
            room.setName(roomDetails.getName());
            room.setFloor(roomDetails.getFloor());
            room.setCapacity(roomDetails.getCapacity());
            room.setFacilities(roomDetails.getFacilities());
            if (roomDetails.getStatus() != null) {
                room.setStatus(roomDetails.getStatus());
            }
            return ResponseEntity.ok(roomService.saveRoom(room));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        String newStatus = statusMap.get("status");
        return roomService.getRoomById(id).map(room -> {
            room.setStatus(newStatus != null ? newStatus : ("Active".equals(room.getStatus()) ? "Inactive" : "Active"));
            return ResponseEntity.ok(roomService.saveRoom(room));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        return roomService.getRoomById(id).map(room -> {
            roomService.deleteRoom(id);
            return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
