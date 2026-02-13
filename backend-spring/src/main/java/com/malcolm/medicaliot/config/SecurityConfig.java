package com.malcolm.medicaliot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // Enable CORS
                .csrf(csrf -> csrf.disable()) // Disable CSRF for development/testing
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Enable H2 console frames
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow Auth endpoints
                        .requestMatchers("/api/sensor/**").permitAll() // Allow Sensor uploads
                        .requestMatchers("/api/patients/**").permitAll() // Allow Patient list for dashboard
                        .requestMatchers("/api/consent/**").permitAll() // Allow Consent management
                        .requestMatchers("/api/security/**").permitAll() // Allow Security status
                        .requestMatchers("/api/performance/**").permitAll() // Allow Performance metrics
                        .requestMatchers("/api/emergency/**").permitAll() // Allow Emergency override
                        .requestMatchers("/api/export/**").permitAll() // Allow Data export
                        .requestMatchers("/api/admin/sessions/**").permitAll() // Allow Session monitoring
                        .requestMatchers("/api/doctor/**").permitAll() // Allow Doctor availability & appointments
                        .requestMatchers("/api/patient/**").permitAll() // Allow Patient appointment booking
                        .requestMatchers("/ws-vitals/**").permitAll() // Allow WebSocket handshake
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(5) // Allow up to 5 simultaneous sessions per user
                        .sessionRegistry(sessionRegistry()))
                .httpBasic(withDefaults()); // Use Basic Auth for simplicity in Phase 1

        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*") // Allow all origins for local network access
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
