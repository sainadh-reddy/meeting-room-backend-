package com.meetingroom.monolith;

import com.meetingroom.monolith.auth.User;
import com.meetingroom.monolith.auth.UserRepository;
import com.meetingroom.monolith.room.Room;
import com.meetingroom.monolith.room.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
@EnableAsync
public class MonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolithApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Admin User
            if (userRepository.findByEmail("admin@meetingroom.com").isEmpty()) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@meetingroom.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }

            // Seed Employee User
            if (userRepository.findByEmail("alice@meetingroom.com").isEmpty()) {
                User employee = new User();
                employee.setName("Alice Smith");
                employee.setEmail("alice@meetingroom.com");
                employee.setPassword(passwordEncoder.encode("Employee@123"));
                employee.setRole("EMPLOYEE");
                userRepository.save(employee);
            }

            // Seed Initial Rooms if none exist
            if (roomRepository.count() == 0) {
                Room r1 = new Room(null, "Apollo Boardroom", 1, 16, "Projector, Video Conf, Whiteboard", "Active");
                Room r2 = new Room(null, "Brainstorm Hub", 1, 6, "Whiteboard, TV Display", "Active");
                Room r3 = new Room(null, "Executive Suite", 2, 24, "Projector, Video Conf, Sound System, Catering", "Active");
                Room r4 = new Room(null, "Innovation Pod", 2, 4, "TV Display, Smart Board", "Active");
                Room r5 = new Room(null, "Zen Focus Room", 3, 2, "Whiteboard", "Active");
                roomRepository.saveAll(List.of(r1, r2, r3, r4, r5));
            }
        };
    }
}
