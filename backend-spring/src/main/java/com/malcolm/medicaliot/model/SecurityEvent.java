package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

    public SecurityEvent() {
    }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTriggeredByIp() {
        return triggeredByIp;
    }

    public void setTriggeredByIp(String triggeredByIp) {
        this.triggeredByIp = triggeredByIp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
