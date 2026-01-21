package com.malcolm.medicaliot.dto;

import lombok.Data;

@Data
public class SensorDataDto {
    private String patientId;
    private int heartRate;
    private int spo2;
    private float temperature;
    private int systolicBP;
    private int diastolicBP;
}
