package com.malcolm.mcpserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patient_consent")
@Data
@NoArgsConstructor
public class PatientConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String patientId;

    @Column(nullable = false)
    private String doctorId;

    private String status;

    private String policyToken;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        policyToken = "CONSENT_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
