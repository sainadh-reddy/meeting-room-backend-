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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolithApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // User credentials initialization if missing
            if (userRepository.findByEmail("admin@meetingroom.com").isEmpty()) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@meetingroom.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("alice@meetingroom.com").isEmpty()) {
                User employee = new User();
                employee.setName("Alice Smith");
                employee.setEmail("alice@meetingroom.com");
                employee.setPassword(passwordEncoder.encode("Employee@123"));
                employee.setRole("EMPLOYEE");
                userRepository.save(employee);
            }
        };
    }
}

