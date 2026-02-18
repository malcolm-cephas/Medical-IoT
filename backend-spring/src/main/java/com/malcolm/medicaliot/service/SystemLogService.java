package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.SystemLog;
import com.malcolm.medicaliot.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository repository;

    public void log(String username, String action, String description, String status) {
        SystemLog log = new SystemLog(username, action, description, status);
        repository.save(log);
    }

    public List<SystemLog> getAllLogs() {
        return repository.findAllByOrderByTimestampDesc();
    }
}
