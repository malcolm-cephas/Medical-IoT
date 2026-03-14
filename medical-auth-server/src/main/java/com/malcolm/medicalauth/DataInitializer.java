package com.malcolm.medicalauth;

import com.malcolm.medicalauth.model.User;
import com.malcolm.medicalauth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Bean
    public CommandLineRunner initAuthData(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            // Seed Doctor Accounts
            seedUser(userRepository, encoder, "doctor_micheal", "DOCTOR", "CARDIOLOGY");
            seedUser(userRepository, encoder, "dr_smith", "DOCTOR", "CARDIOLOGY");
            seedUser(userRepository, encoder, "dr_johnson", "DOCTOR", "DERMATOLOGY");
            seedUser(userRepository, encoder, "dr_lee", "DOCTOR", "PEDIATRICS");
            seedUser(userRepository, encoder, "dr_davis", "DOCTOR", "ORTHOPEDICS");
            seedUser(userRepository, encoder, "dr_patel", "DOCTOR", "GENERAL_PRACTITIONER");

            // Seed Admin and Nurse
            seedUser(userRepository, encoder, "admin", "ADMIN", "SYSTEM");
            seedUser(userRepository, encoder, "nurse_jane", "NURSE", "GENERAL");

            // Seed Demo Patient
            seedUser(userRepository, encoder, "patient_001", "PATIENT", "GENERAL");
            
            System.out.println("AUTH_SERVER: Demo accounts seeded successfully.");
        };
    }

    private void seedUser(UserRepository repo, PasswordEncoder encoder, String username, String role, String dept) {
        if (repo.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(encoder.encode("password"));
            u.setRole(role);
            u.setDepartment(dept);
            repo.save(u);
        }
    }
}
