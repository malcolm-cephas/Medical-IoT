package com.malcolm.medicaliot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for development/testing
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Enable H2 console frames
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow Auth endpoints
                        .requestMatchers("/api/sensor/**").permitAll() // Allow Sensor uploads (simulate no-auth or use
                                                                       // token later)
                        .requestMatchers("/api/patients/**").permitAll() // Allow Patient list for dashboard
                        .requestMatchers("/api/consent/**").permitAll() // Allow Consent management
                        .anyRequest().authenticated())
                .httpBasic(withDefaults()); // Use Basic Auth for simplicity in Phase 1

        return http.build();
    }
}
