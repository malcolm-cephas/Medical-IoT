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
                User u = new User(null, "doctor_micheal", encoder.encode("password"), "DOCTOR", "CARDIOLOGY",
                        "doctor,cardiology");
                u.setFullName("Dr. Micheal Scott");
                u.setAge(45);
                u.setGender("M");
                userRepository.save(u);
            }

            // 2. Create Nurse
            if (userRepository.findByUsername("nurse_jane").isEmpty()) {
                User u = new User(null, "nurse_jane", encoder.encode("password"), "NURSE", "GENERAL", "nurse");
                u.setFullName("Nurse Jane Doe");
                u.setAge(32);
                u.setGender("F");
                userRepository.save(u);
            }

            // 3. Create Admin
            if (userRepository.findByUsername("admin").isEmpty()) {
                User u = new User(null, "admin", encoder.encode("password"), "ADMIN", "SYSTEM", "admin");
                u.setFullName("System Administrator");
                u.setAge(30);
                u.setGender("M");
                userRepository.save(u);
            }

            // 4. Create Specific Test Patients and Initial Data
            String[] testPatients = { "alpha", "beta", "gamma", "patient_alpha", "patient_beta", "patient_gamma" };
            String[] commonNames = { "Alice Smith", "Bob Jones", "Charlie Brown", "David Wilson", "Eva Green",
                    "Frank White" };

            for (int k = 0; k < testPatients.length; k++) {
                String username = testPatients[k];
                if (userRepository.findByUsername(username).isEmpty()) {
                    User u = new User(null, username, encoder.encode("password"), "PATIENT", "GENERAL", "patient");
                    u.setFullName(commonNames[k]);
                    u.setAge(20 + random.nextInt(60));
                    u.setGender(random.nextBoolean() ? "M" : "F");
                    userRepository.save(u);
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
            String[] firstNames = { "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
                    "William", "Elizabeth" };
            String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                    "Rodriguez", "Martinez" };

            for (int i = 1; i <= 35; i++) {
                String username = "patient_" + String.format("%03d", i);
                if (userRepository.findByUsername(username).isEmpty()) {
                    User u = new User(null, username, encoder.encode("password"), "PATIENT", "GENERAL", "patient");

                    String fn = firstNames[random.nextInt(firstNames.length)];
                    String ln = lastNames[random.nextInt(lastNames.length)];
                    u.setFullName(fn + " " + ln);
                    u.setAge(18 + random.nextInt(70));
                    u.setGender(random.nextBoolean() ? "M" : "F");

                    userRepository.save(u);

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
