package com.malcolm.medicalauth.service;

import com.malcolm.medicalauth.model.User;
import com.malcolm.medicalauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



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
    public void enroll(String username, String descriptorJson, String imageBase64) {
        System.out.println("AUTH_LOG: Initializing AI Biometric Mapping for: " + username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in Auth DB: " + username));
        
        // Save the 128-d Vector (for fast Java-side comparisons if needed)
        user.setFaceDescriptor(descriptorJson);
        
        // Save the Raw Binary Image (for Python AI post-processing and recovery)
        if (imageBase64 != null && imageBase64.contains(",")) {
            try {
                String pureBase64 = imageBase64.split(",")[1];
                byte[] imageBytes = java.util.Base64.getDecoder().decode(pureBase64);
                user.setProfileImage(imageBytes);
                System.out.println("AUTH_LOG: Binary Profile Image stored for user: " + username);
            } catch (Exception e) {
                System.err.println("AUTH_LOG: WARN - Failed to decode/store binary image: " + e.getMessage());
            }
        }
        
        userRepository.save(user);
        System.out.println("AUTH_LOG: Biometrics and Identity Mapping complete for: " + username);
    }

    public boolean hasBiometrics(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getFaceDescriptor() != null && !u.getFaceDescriptor().isEmpty())
                .orElse(false);
    }

    public boolean verify(String username, String providedDescriptor) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in Auth DB: " + username));

        String storedDescriptor = user.getFaceDescriptor();
        if (storedDescriptor == null) return false;

        try {
            // Call Python Analytics Service (Port 4242) for LBPH comparison
            String analyticsUrl = "http://localhost:4242/biometric/verify";
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            java.util.Map<String, Object> request = new java.util.HashMap<>();
            request.put("stored_descriptor_b64", storedDescriptor);
            request.put("captured_b64", providedDescriptor);

            @SuppressWarnings("rawtypes")
            org.springframework.http.ResponseEntity<java.util.Map> response = 
                restTemplate.postForEntity(analyticsUrl, request, java.util.Map.class);
            
            java.util.Map responseBody = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                Boolean isValid = (Boolean) responseBody.get("valid");
                return Boolean.TRUE.equals(isValid);
            }
            return false;
        } catch (Exception e) {
            System.err.println("AUTH_LOG: Python verification failed: " + e.getMessage());
            return false;
        }
    }
}
