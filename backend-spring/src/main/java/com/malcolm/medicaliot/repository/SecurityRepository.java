package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecurityRepository extends JpaRepository<SecurityEvent, Long> {
    List<SecurityEvent> findByEventType(String eventType);

    List<SecurityEvent> findTop10ByOrderByTimestampDesc();
}
