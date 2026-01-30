package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "patient_consent")
public class PatientConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String patientId; // User who owns the data

    @Column(nullable = false)
    private String doctorId; // User who wants access

    private String status; // PENDING, APPROVED, REJECTED

    private String policyToken; // Unique token included in encryption policy

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        // Generate a random token for ABE policy integration
        // Policy will look like: (Role:Doctor AND Consent:TOKEN_123)
        policyToken = "CONSENT_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
