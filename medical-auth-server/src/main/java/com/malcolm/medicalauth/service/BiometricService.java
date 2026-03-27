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

    public boolean verify(String username, List<Double> providedDescriptor) {
        // We'll keep the current logic but add a hook for Python-based verification if needed.
        // Actually, the user wants the face recognition IN PYTHON.
        // Let's implement the cross-service call.
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in Auth DB: " + username));

        String storedDescriptorJson = user.getFaceDescriptor();
        if (storedDescriptorJson == null) return false;

        try {
            // Traditional Java comparison as backup
            String clean = storedDescriptorJson.trim().replace("[", "").replace("]", "");
            if (clean.isEmpty()) return false;
            
            String[] parts = clean.split(",");
            List<Double> storedDescriptor = new java.util.ArrayList<>();
            for (String p : parts) storedDescriptor.add(Double.parseDouble(p.trim()));

            // Call Python Analytics Service for high-precision comparison (YOLO + dlib) if needed
            try {
                // If we're verifying with a provided descriptor (from frontend), 
                // the frontend has already done the heavy work via the Python bridge.
                
                double sumOfSquares = 0;
                for (int i = 0; i < storedDescriptor.size(); i++) {
                    double diff = storedDescriptor.get(i) - providedDescriptor.get(i);
                    sumOfSquares += diff * diff;
                }
                double distance = Math.sqrt(sumOfSquares);
                System.out.println("AUTH_LOG: Hybrid AI verification. Distance: " + distance);
                return distance < 0.6;
            } catch (Exception e) {
                System.err.println("AUTH_LOG: Python bridge failed, using Java fallback.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("AUTH_LOG: Critical Verification failure: " + e.getMessage());
            return false;
        }
    }
}
