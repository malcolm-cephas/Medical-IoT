package com.malcolm.medicaliot;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.UserRepository;
import com.malcolm.medicaliot.dto.SensorDataDto;
import com.malcolm.medicaliot.controller.SensorController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, SensorController sensorController) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // 1. Create Doctor if not exists
            if (userRepository.findByUsername("doctor_micheal").isEmpty()) {
                userRepository.save(new User(null, "doctor_micheal", encoder.encode("password"), "DOCTOR", "CARDIOLOGY",
                        "doctor,cardiology"));
            }

            // 2. Create Nurse if not exists
            if (userRepository.findByUsername("nurse_jane").isEmpty()) {
                userRepository
                        .save(new User(null, "nurse_jane", encoder.encode("password"), "NURSE", "GENERAL", "nurse"));
            }

            // 3. Create Admin if not exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User(null, "admin", encoder.encode("password"), "ADMIN", "SYSTEM", "admin"));
            }

            // 4. Create 35 Patient accounts
            Random random = new Random();
            for (int i = 1; i <= 35; i++) {
                String username = "patient_" + String.format("%03d", i);
                if (userRepository.findByUsername(username).isEmpty()) {
                    userRepository.save(
                            new User(null, username, encoder.encode("password"), "PATIENT", "GENERAL", "patient"));

                    // Simulate some initial sensor data for each patient
                    for (int j = 0; j < 5; j++) {
                        SensorDataDto data = new SensorDataDto();
                        data.setPatientId(username);
                        data.setHeartRate(60 + random.nextInt(40));
                        data.setSpo2(94 + random.nextInt(6));
                        data.setTemperature((float) (36.0 + random.nextDouble() * 2.0));
                        sensorController.uploadData(data);
                    }
                }
            }

            System.out.println("--- SYSTEM INITIALIZED WITH 35 PATIENTS, 1 DOCTOR, 1 NURSE ---");
        };
    }
}
