package com.malcolm.medicalauth.controller;

import com.malcolm.medicalauth.dto.AuthResponse;
import com.malcolm.medicalauth.model.User;
import com.malcolm.medicalauth.repository.UserRepository;
import com.malcolm.medicalauth.security.JwtService;
import com.malcolm.medicalauth.service.LockdownService;
import com.malcolm.medicalauth.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LockdownService lockdownService;

    @Autowired
    private SystemLogService logService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logService.log(savedUser.getUsername(), "REGISTER", "New user registered", "SUCCESS");
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403).body("System is in lockdown: " + lockdownService.getReason());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
        } catch (Exception e) {
            lockdownService.recordFailedLogin(user.getUsername(), "127.0.0.1");
            logService.log(user.getUsername(), "LOGIN", "Failed login", "FAILURE");
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        User dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow();
        String token = jwtService.generateToken(dbUser.getUsername(), dbUser.getRole());
        lockdownService.resetFailedLogin(dbUser.getUsername());
        logService.log(dbUser.getUsername(), "LOGIN", "Successful login", "SUCCESS");

        return ResponseEntity.ok(new AuthResponse(token, dbUser));
    }
}
