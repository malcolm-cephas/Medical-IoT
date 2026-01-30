package com.malcolm.medicaliot.config;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        if (userService.findByUsername("doctor").isEmpty()) {
            userService
                    .registerUser(new User(null, "doctor", "doctor123", "doctor", "Cardiology", "doctor,cardiology"));
            System.out.println("Seeded Doctor user.");
        }

        if (userService.findByUsername("nurse").isEmpty()) {
            userService.registerUser(new User(null, "nurse", "nurse123", "nurse", "Emergency", "nurse,emergency"));
            System.out.println("Seeded Nurse user.");
        }

        if (userService.findByUsername("patient_alpha").isEmpty()) {
            userService.registerUser(
                    new User(null, "patient_alpha", "patient123", "patient", "Cardiology", "patient,cardiology"));
            System.out.println("Seeded Patient Alpha.");
        }

        if (userService.findByUsername("patient_beta").isEmpty()) {
            userService.registerUser(
                    new User(null, "patient_beta", "patient123", "patient", "General", "patient,general"));
            System.out.println("Seeded Patient Beta.");
        }

        if (userService.findByUsername("patient_gamma").isEmpty()) {
            userService.registerUser(
                    new User(null, "patient_gamma", "patient123", "patient", "Pediatrics", "patient,pediatrics"));
            System.out.println("Seeded Patient Gamma.");
        }
    }
}
