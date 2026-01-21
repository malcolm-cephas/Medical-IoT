package com.malcolm.medicaliot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

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
                        .requestMatchers(PathRequest.toH2Console()).permitAll() // Allow H2 Console
                        .requestMatchers("/api/auth/**").permitAll() // Allow Auth endpoints
                        .requestMatchers("/api/sensor/**").permitAll() // Allow Sensor uploads (simulate no-auth or use
                                                                       // token later)
                        .anyRequest().authenticated())
                .httpBasic(withDefaults()); // Use Basic Auth for simplicity in Phase 1

        return http.build();
    }
}
