package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_verification_sessions")
public class FaceVerificationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private boolean active;

    public FaceVerificationSession() {}

    public FaceVerificationSession(String username, LocalDateTime verifiedAt, boolean active) {
        this.username = username;
        this.verifiedAt = verifiedAt;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(verifiedAt.plusMinutes(5));
    }
}
