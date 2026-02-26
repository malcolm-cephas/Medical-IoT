package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a single data point of patient vitals collected from IoT
 * sensors.
 * Stores physiological metrics like Heart Rate, SpO2, and Environmental data.
 */
@Entity
@Table(name = "sensor_data")
public class SensorData {

    /**
     * Unique ID for the sensor reading record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ID of the patient to whom this data belongs.
     * Cannot be null.
     */
    @Column(nullable = false)
    private String patientId;

    // --- Vital Signs ---

    private int heartRate; // Beats Per Minute (BPM)
    private int spo2; // Oxygen Saturation (%)
    private float temperature; // Body Temperature (Celsius)
    private int systolicBP; // Systolic Blood Pressure (mmHg)
    private int diastolicBP; // Diastolic Blood Pressure (mmHg)

    // --- Environmental Context ---

    private float humidity; // Room Humidity (%) - useful for respiratory context

    /**
     * Timestamp when the data was received/recorded.
     */
    private LocalDateTime timestamp;

    // Default constructor for JPA
    public SensorData() {
    }

    /**
     * Parameterized constructor for creating new sensor data instances.
     */
    public SensorData(Long id, String patientId, int heartRate, int spo2, float temperature, int systolicBP,
            int diastolicBP, float humidity, LocalDateTime timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.temperature = temperature;
        this.systolicBP = systolicBP;
        this.diastolicBP = diastolicBP;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    /**
     * Lifecycle callback to set the timestamp automatically before persisting to
     * the database.
     */
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
