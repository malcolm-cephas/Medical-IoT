package com.malcolm.medicaliot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuration class for Spring Security.
 * This class defines how the application handles authentication, authorization,
 * CORS, and CSRF protection.
 */
@Configuration // Marks this class as a source of bean definitions for the application context.
@EnableWebSecurity // Enables Spring Security's web security support and provides the Spring MVC
                   // integration.
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity // Enables annotation-based
                                                                                          // security (e.g.,
                                                                                          // @PreAuthorize) on methods.
public class SecurityConfig {

    /**
     * Custom filter for JWT (JSON Web Token) authentication.
     * This filter intercepts requests to validate the JWT token.
     */
    @Autowired
    private com.malcolm.medicaliot.security.JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Service to load user-specific data during authentication.
     */
    @Autowired
    private com.malcolm.medicaliot.security.CustomUserDetailsService userDetailsService;

    /**
     * Configures the security filter chain.
     * This acts as the main entry point for defining security rules.
     *
     * @param http the HttpSecurity object to configure.
     * @return the configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/sensor/**").permitAll()
                        .requestMatchers("/ws-vitals/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // new change
                        .requestMatchers("/api/watermark/**").permitAll()
                        .requestMatchers("/api/medical-records/**").permitAll()
                        // till here
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates and configures the AuthenticationProvider.
     * It uses a DaoAuthenticationProvider which retrieves user details from a
     * UserDetailsService.
     *
     * @return the configured AuthenticationProvider.
     */
    @Bean
    public org.springframework.security.authentication.AuthenticationProvider authenticationProvider() {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        // Set the service to load user data (e.g., from database)
        authProvider.setUserDetailsService(userDetailsService);
        // Set the encoder to verify passwords
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * The AuthenticationManager is the main API for authenticating a user request.
     *
     * @param config the AuthenticationConfiguration object.
     * @return the AuthenticationManager instance.
     * @throws Exception if an error occurs.
     */
    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the PasswordEncoder bean.
     * BCrypt is a strong hashing function used here for password security.
     *
     * @return a BCryptPasswordEncoder instance.
     */
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    /**
     * Configures global CORS settings for the application.
     * Allows the frontend (or other clients) to make requests to this backend.
     *
     * @return a WebMvcConfigurer with CORS mappings.
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration
                .setAllowedOriginPatterns(java.util.List.of("http://localhost:5173", "http://127.0.0.1:5173", "*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
