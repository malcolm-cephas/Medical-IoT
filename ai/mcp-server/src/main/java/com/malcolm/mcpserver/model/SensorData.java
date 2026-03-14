package com.malcolm.mcpserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {
    private Long id;
    private String patientId;
    private int heartRate;
    private int spo2;
    private float temperature;
    private int systolicBP;
    private int diastolicBP;
    private String conditionStatus;
    private String clinicalDiagnosis;
    private float humidity;
    private LocalDateTime timestamp;
}
