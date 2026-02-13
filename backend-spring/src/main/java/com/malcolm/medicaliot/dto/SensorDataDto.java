package com.malcolm.medicaliot.dto;

public class SensorDataDto {
    private String patientId;
    private int heartRate;
    private int spo2;
    private float temperature;
    private int systolicBP;
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
