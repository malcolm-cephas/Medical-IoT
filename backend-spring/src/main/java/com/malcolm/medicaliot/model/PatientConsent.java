package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
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

    public PatientConsent() {
    }

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        // Generate a random token for ABE policy integration
        // Policy will look like: (Role:Doctor AND Consent:TOKEN_123)
        policyToken = "CONSENT_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPolicyToken() {
        return policyToken;
    }

    public void setPolicyToken(String policyToken) {
        this.policyToken = policyToken;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
