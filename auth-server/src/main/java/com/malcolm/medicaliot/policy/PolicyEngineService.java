package com.malcolm.medicaliot.policy;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.model.PatientConsent;
import com.malcolm.medicaliot.repository.ConsentRepository;
import com.malcolm.medicaliot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyEngineService {

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Evaluates access based on Role, Department, and Consent.
     * Rule: (role == DOCTOR OR NURSE) AND (department match OR consent == APPROVED)
     * 
     * @param username        The username of the requester
     * @param targetPatientId The patient ID being accessed
     * @param action          The action being performed (READ, WRITE) - unused for
     *                        now but good for ABAC extensibility
     */
    public boolean evaluateAccess(String username, String targetPatientId, String action) {

        // Fetch fresh user details
        User requester = userRepository.findByUsername(username).orElse(null);
        if (requester == null) {
            publishAlert("UNKNOWN_USER_ACCESS", "WARN", "Unknown user tried to access " + targetPatientId, "UNKNOWN");
            return false;
        }

        // 1. Admin Override
        if ("ADMIN".equalsIgnoreCase(requester.getRole())) {
            return true;
        }

        // 2. Self Access
        if (requester.getUsername().equals(targetPatientId)) {
            return true;
        }

        // 3. Role Check
        if (!"DOCTOR".equalsIgnoreCase(requester.getRole()) && !"NURSE".equalsIgnoreCase(requester.getRole())) {
            publishAlert("ROLE_VIOLATION", "WARN",
                    requester.getUsername() + " tried to access " + targetPatientId + " without medical role",
                    requester.getUsername());
            return false;
        }

        // 4. Department Check (e.g. Cardiology doctor accessing any Cardiology patient?
        // For now, we assume patients don't have department fields openly readable
        // without consent)
        // Implementation: Check explicitly granted consent or emergency

        // 5. Explicit Consent Check
        List<PatientConsent> consents = consentRepository.findByPatientId(targetPatientId);
        boolean hasConsent = consents.stream()
                .anyMatch(c -> c.getDoctorId().equals(requester.getUsername()) && "APPROVED".equals(c.getStatus()));

        if (hasConsent) {
            return true;
        }

        // 6. Emergency Override (Check Audit Log? Or separate service?)
        // Assuming EmergencyOverrideService sets a temporary consent or flag.
        // For now, return false if no explicit consent.
        publishAlert("CONSENT_VIOLATION", "WARN",
                "Doctor " + requester.getUsername() + " tried to access " + targetPatientId + " without CONSENT",
                requester.getUsername());
        return false;
    }

    private void publishAlert(String type, String severity, String desc, String users) {
        eventPublisher
                .publishEvent(new com.malcolm.medicaliot.event.SecurityAlertEvent(this, type, severity, desc, users));
    }
}
