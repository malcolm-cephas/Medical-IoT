package com.malcolm.medicaliot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/sessions")
public class SessionMonitoringController {

    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    @GetMapping("/active")
    public List<String> getActiveSessions() {
        if (sessionRegistry == null) {
            return List.of("Session Registry not configured. Please enable in SecurityConfig.");
        }
        return sessionRegistry.getAllPrincipals().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
