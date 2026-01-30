package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Will need to configure bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Use a simple encoder for now, ideally should be a Bean in SecurityConfig
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Revocation Logic
    // Parses the CSV/JSON attribute string, removes the target attribute, and
    // saves.
    public boolean revokeAttribute(String username, String attributeToRemove) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String currentAttrs = user.getAttributes();

            if (currentAttrs != null && currentAttrs.contains(attributeToRemove)) {
                // Simple string manipulation for demo (optimally use proper CSV parsing)
                // Remove attr and clean up commas
                String newAttrs = currentAttrs.replace(attributeToRemove, "")
                        .replace(",,", ",")
                        .replaceAll("^,|,$", ""); // trim leading/trailing commas

                user.setAttributes(newAttrs);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
