package com.malcolm.medicaliot.event;

import org.springframework.context.ApplicationEvent;

public class SecurityAlertEvent extends ApplicationEvent {
    private final String eventType;
    private final String severity;
    private final String description;
    private final String username;

    public SecurityAlertEvent(Object source, String eventType, String severity, String description, String username) {
        super(source);
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.username = username;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }
}
