package com.malcolm.medicaliot;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.model.SensorData;
import com.malcolm.medicaliot.repository.UserRepository;
import com.malcolm.medicaliot.repository.SensorDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, SensorDataRepository sensorDataRepository) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            Random random = new Random();

            // 1. Create Doctor
            if (userRepository.findByUsername("doctor_micheal").isEmpty()) {
                userRepository.save(new User(null, "doctor_micheal", encoder.encode("password"), "DOCTOR", "CARDIOLOGY",
                        "doctor,cardiology"));
            }

            // 2. Create Nurse
            if (userRepository.findByUsername("nurse_jane").isEmpty()) {
                userRepository
                        .save(new User(null, "nurse_jane", encoder.encode("password"), "NURSE", "GENERAL", "nurse"));
            }

            // 3. Create Admin
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User(null, "admin", encoder.encode("password"), "ADMIN", "SYSTEM", "admin"));
            }

            // 4. Create Specific Test Patients and Initial Data
            String[] testPatients = { "alpha", "beta", "gamma", "patient_alpha", "patient_beta", "patient_gamma" };
            for (String username : testPatients) {
                if (userRepository.findByUsername(username).isEmpty()) {
                    userRepository.save(
                            new User(null, username, encoder.encode("password"), "PATIENT", "GENERAL", "patient"));
                }

                // Seed some initial data if none exists
                if (sensorDataRepository.findByPatientIdOrderByTimestampAsc(username).isEmpty()) {
                    for (int j = 0; j < 10; j++) {
                        SensorData data = new SensorData();
                        data.setPatientId(username);
                        data.setHeartRate(70 + random.nextInt(20));
                        data.setSpo2(95 + random.nextInt(5));
                        data.setTemperature(36.5f + random.nextFloat());
                        data.setHumidity(40.0f + random.nextFloat() * 20);
                        data.setSystolicBP(110 + random.nextInt(20));
                        data.setDiastolicBP(70 + random.nextInt(15));
                        data.setTimestamp(LocalDateTime.now().minusMinutes(10 - j));
                        sensorDataRepository.save(data);
                    }
                }
            }

            // 5. Create 35 General Patient accounts
            for (int i = 1; i <= 35; i++) {
                String username = "patient_" + String.format("%03d", i);
                if (userRepository.findByUsername(username).isEmpty()) {
                    userRepository.save(
                            new User(null, username, encoder.encode("password"), "PATIENT", "GENERAL", "patient"));

                    // Initial vitals for the ward list
                    SensorData data = new SensorData();
                    data.setPatientId(username);
                    data.setHeartRate(60 + random.nextInt(40));
                    data.setSpo2(94 + random.nextInt(6));
                    data.setTemperature(36.0f + random.nextFloat() * 2);
                    data.setHumidity(45.0f);
                    data.setSystolicBP(120);
                    data.setDiastolicBP(80);
                    sensorDataRepository.save(data);
                }
            }

            System.out.println("--- SYSTEM INITIALIZED: 40+ PATIENTS, 1 DOCTOR, 1 NURSE, 1 ADMIN ---");
        };
    }
}
