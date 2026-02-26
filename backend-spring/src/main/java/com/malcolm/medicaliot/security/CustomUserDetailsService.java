package com.malcolm.medicaliot.security;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

/**
 * Service implementation to load user-specific data during authentication.
 * Used by Spring Security to bridge the custom User entity with Spring's
 * UserDetails.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads the user by username from the database.
     * 
     * @param username The username identifying the user whose data is required.
     * @return A UserDetails object acting as the principal for the authenticated
     *         user.
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Create authorities (roles) list for the user
        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            // Spring Security expects roles to potentially start with "ROLE_"
            // Here we convert "DOCTOR" -> "ROLE_DOCTOR"
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_" + user.getRole().toUpperCase()));
        }

        // Return a Spring Security User object (which implements UserDetails)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}
