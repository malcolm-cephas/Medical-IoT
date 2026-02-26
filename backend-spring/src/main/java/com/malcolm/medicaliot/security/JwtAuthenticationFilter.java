package com.malcolm.medicaliot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.lang.NonNull;

/**
 * Filter that executes once per request to validate JWT tokens.
 * It intercepts HTTP requests, extracts the JWT (if present), validates it,
 * and sets up the Spring Security context if the token is valid.
 */
@Component // Marks this class as a Spring Bean so it can be injected into the security
           // chain.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService; // Service to handle JWT operations (extraction, validation)

    @Autowired
    private CustomUserDetailsService userDetailsService; // Service to load user details from the database

    /**
     * The main filter logic.
     * 
     * @param request     The incoming HTTP request.
     * @param response    The HTTP response.
     * @param filterChain The chain of filters to proceed execution.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Get the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Check if the header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If not, continue the filter chain without adding authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token (substring after "Bearer ")
        jwt = authHeader.substring(7);
        try {
            // Extract username from the token using JwtService
            username = jwtService.extractUsername(jwt);

            // If username is valid and no authentication is currently set in the context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate the token against the user details
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    // Create an authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // Set additional details (like IP address, session ID)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication in the SecurityContext
                    // This tells Spring Security that the user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalid or expired - just continue chain.
            // Spring Security will eventually reject the request if the endpoint requires
            // authentication.
            // 401 Unauthorized might be returned by the framework later.
        }

        // Continue with the remaining filters in the chain
        filterChain.doFilter(request, response);
    }
}
