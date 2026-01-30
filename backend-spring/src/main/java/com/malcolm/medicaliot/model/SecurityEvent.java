package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType; // e.g., FAILED_LOGIN, LOCKDOWN_ENABLED, INTRUSION_DETECTED

    @Column(nullable = false)
    private String severity; // HIGH, CRITICAL, INFO

    private String description;

    private String triggeredByIp;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
