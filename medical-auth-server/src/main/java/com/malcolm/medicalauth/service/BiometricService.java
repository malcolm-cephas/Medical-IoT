package com.malcolm.medicalauth.service;

import com.malcolm.medicalauth.model.User;
import com.malcolm.medicalauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to handle biometric logic in the dedicated Auth Server.
 */
@Service
public class BiometricService {

    private final UserRepository userRepository;

    @Autowired
    public BiometricService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void enroll(String username, String descriptorJson) {
        System.out.println("AUTH_LOG: Enrolling biometrics for: " + username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in Auth DB: " + username));
        
        user.setFaceDescriptor(descriptorJson);
        userRepository.save(user);
        
        System.out.println("AUTH_LOG: Biometrics successfully mapped for: " + username);
    }

    public boolean hasBiometrics(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getFaceDescriptor() != null && !u.getFaceDescriptor().isEmpty())
                .orElse(false);
    }

    public boolean verify(String username, List<Double> providedDescriptor) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in Auth DB: " + username));

        String storedDescriptorJson = user.getFaceDescriptor();
        if (storedDescriptorJson == null) return false;

        try {
            String clean = storedDescriptorJson.trim()
                    .replace("[", "")
                    .replace("]", "");
            
            if (clean.isEmpty()) return false;
            
            String[] parts = clean.split(",");
            if (parts.length != providedDescriptor.size()) return false;

            double sumOfSquares = 0;
            for (int i = 0; i < parts.length; i++) {
                double diff = Double.parseDouble(parts[i].trim()) - providedDescriptor.get(i);
                sumOfSquares += diff * diff;
            }
            double distance = Math.sqrt(sumOfSquares);
            
            System.out.println("AUTH_LOG: Biometric distance for " + username + ": " + distance);
            return distance < 0.6;
        } catch (Exception e) {
            System.err.println("AUTH_LOG: Verification Error: " + e.getMessage());
            return false;
        }
    }
}
