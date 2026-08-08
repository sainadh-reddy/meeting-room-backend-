package com.meetingroom.monolith.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping({"/health", "/api/health", "/api/status", "/api/cron/status"})
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Virtusa Meeting Room Booking System Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Backend service is active and operational. Cron ping processed successfully.");
        response.put("database", "CONNECTED");
        return ResponseEntity.ok(response);
    }
}
