package com.malcolm.medicaliot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SensorDataDto {
    private String patientId;

    @Min(0)
    @Max(300)
    private int heartRate;

    @Min(0)
    @Max(100)
    private int spo2;

    @Min(0)
    @Max(50)
    private float temperature;

    @Min(0)
    @Max(300)
    private int systolicBP;

    @Min(0)
    @Max(200)
    private int diastolicBP;

    private float humidity;

    public SensorDataDto() {
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(int systolicBP) {
        this.systolicBP = systolicBP;
    }

    public int getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(int diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    @Override
    public String toString() {
        return "SensorDataDto{" +
                "patientId='" + patientId + '\'' +
                ", heartRate=" + heartRate +
                ", spo2=" + spo2 +
                ", temperature=" + temperature +
                ", systolicBP=" + systolicBP +
                ", diastolicBP=" + diastolicBP +
                ", humidity=" + humidity +
                '}';
    }
}
