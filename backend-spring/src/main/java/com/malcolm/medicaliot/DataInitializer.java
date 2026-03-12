package com.malcolm.medicaliot;

import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.repository.DoctorAvailabilityRepository;
import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.model.SensorData;
import com.malcolm.medicaliot.repository.UserRepository;
import com.malcolm.medicaliot.repository.SensorDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.List;

@Component
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, SensorDataRepository sensorDataRepository,
            DoctorAvailabilityRepository availabilityRepository) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            Random random = new Random();

            String[] wards = { "General Ward", "ICU", "Cardiology Ward", "Pediatric Ward", "Orthopedic Ward" };
            String[] reasons = { "Regular Checkup", "Chest Pain", "Fracture", "High Fever", "Routine Monitoring" };
            String[] referrers = { "Self", "General Practitioner", "Emergency Room", "Outpatient Clinic" };
            String[] diagnoses = { "Healthy", "Mild Hypertension", "Tachycardia", "Recovering from Surgery", "Bilateral Pneumonia" };
            String[] cities = { "New York", "London", "Tokyo", "Mumbai", "Berlin", "Paris" };

            // 1. Create Doctor
            if (userRepository.findByUsername("doctor_micheal").isEmpty()) {
                User u = new User(null, "doctor_micheal", encoder.encode("password"), "DOCTOR", "CARDIOLOGY",
                        "doctor,cardiology");
                u.setFullName("Dr. Micheal Scott");
                u.setAge(45);
                u.setGender("M");
                u.setSpecialization("Interventional Cardiology");
                u.setClearanceLevel("L3");
                u.setAddress("123 Heart St, " + cities[random.nextInt(cities.length)]);
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
                        data.setConditionStatus(random.nextInt(10) > 8 ? "CRITICAL" : "STABLE");
                        data.setClinicalDiagnosis(diagnoses[random.nextInt(diagnoses.length)]);
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
                    u.setAddress(random.nextInt(999) + " Patient Ln, " + cities[random.nextInt(cities.length)]);
                    u.setWardName(wards[random.nextInt(wards.length)]);
                    u.setWardNumber(100 + random.nextInt(900));
                    u.setReferredBy(referrers[random.nextInt(referrers.length)]);
                    u.setReasonOfAdmission(reasons[random.nextInt(reasons.length)]);
                    u.setDateOfAdmission(LocalDateTime.now().minusDays(random.nextInt(10)));

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
                    data.setConditionStatus("STABLE");
                    data.setClinicalDiagnosis("Normal Baseline");
                    sensorDataRepository.save(data);
                }
            }

            // 6. Create Requested Doctors
            createDoctor(userRepository, availabilityRepository, encoder,
                    "dr_smith", "Dr. Smith", "CARDIOLOGY",
                    List.of("MONDAY", "WEDNESDAY", "FRIDAY"), "09:00:00", "17:00:00");

            createDoctor(userRepository, availabilityRepository, encoder,
                    "dr_johnson", "Dr. Johnson", "DERMATOLOGY",
                    List.of("TUESDAY", "THURSDAY"), "10:00:00", "18:00:00");

            createDoctor(userRepository, availabilityRepository, encoder,
                    "dr_lee", "Dr. Lee", "PEDIATRICS",
                    List.of("MONDAY", "WEDNESDAY", "FRIDAY"), "08:00:00", "16:00:00");

            createDoctor(userRepository, availabilityRepository, encoder,
                    "dr_davis", "Dr. Davis", "ORTHOPEDICS",
                    List.of("TUESDAY", "THURSDAY"), "09:00:00", "17:00:00");

            createDoctor(userRepository, availabilityRepository, encoder,
                    "dr_patel", "Dr. Patel", "GENERAL_PRACTITIONER",
                    List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"), "08:00:00", "18:00:00");

            System.out.println("--- SYSTEM INITIALIZED: 40+ PATIENTS, 6 DOCTORS, 1 NURSE, 1 ADMIN ---");
        };
    }

    private void createDoctor(UserRepository userRepository,
            DoctorAvailabilityRepository availabilityRepository,
            BCryptPasswordEncoder encoder,
            String username, String fullName, String specialty,
            List<String> days, String start, String end) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User u = new User(null, username, encoder.encode("password"), "DOCTOR", specialty,
                    "doctor," + specialty.toLowerCase());
            u.setFullName(fullName);
            u.setAge(35 + new Random().nextInt(20));
            u.setGender(new Random().nextBoolean() ? "M" : "F");
            u.setSpecialization(specialty);
            u.setClearanceLevel("L2");
            u.setAddress(new Random().nextInt(500) + " Medical Plaza, New York");
            User saved = userRepository.save(u);

            LocalTime startTime = LocalTime.parse(start);
            LocalTime endTime = LocalTime.parse(end);

            for (String day : days) {
                availabilityRepository.save(new DoctorAvailability(saved.getId(), day, startTime, endTime));
            }
        }
    }
}
