package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a medical Prescription entity in the database.
 * This class maps to the "prescriptions" table and stores details about
 * medications prescribed by a doctor to a patient.
 */
@Entity // Specifies that this class is an entity and is mapped to a database table.
@Data // Lombok annotation to automatically generate getters, setters, toString,
      // equals, and hashCode methods.
@Table(name = "prescriptions") // Specifies the name of the database table to be used for mapping.
public class Prescription {

    /**
     * Unique identifier for the prescription.
     * Calculated automatically by the database (Identity strategy).
     */
    @Id // Marks this field as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indicates that the persistence provider must assign primary
                                                        // keys for the entity using a database identity column.
    private Long id;

    /**
     * The ID of the doctor who issued the prescription.
     * This is a foreign key reference conceptually, though mapped here as a simple
     * Long.
     * Cannot be null.
     */
    @Column(name = "doctor_id", nullable = false) // Maps this field to the "doctor_id" column in the table.
    private Long doctorId;

    /**
     * The ID of the patient for whom the prescription is issued.
     * Cannot be null.
     */
    @Column(name = "patient_id", nullable = false) // Maps this field to the "patient_id" column in the table.
    private Long patientId;

    /**
     * The medical diagnosis given by the doctor.
     * Cannot be null.
     */
    @Column(nullable = false) // Standard column mapping, non-nullable.
    private String diagnosis;

    /**
     * Details of the medicine prescribed.
     * Stored as TEXT to allow for longer strings (e.g., multiple medicines,
     * dosages).
     * Example: "Paracetamol 500mg - 1-0-1"
     */
    @Column(columnDefinition = "TEXT") // Specifies that the column type in the database should be TEXT (for large
                                       // strings).
    private String medicine;

    /**
     * Additional notes or advice for the patient.
     * Also stored as TEXT for flexibility.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * The timestamp when the prescription was created.
     */
    @Column(name = "prescribed_date")
    private LocalDateTime prescribedDate;

    /**
     * Lifecycle callback method that runs before the entity is persisted (saved) to
     * the database.
     * Used here to automatically set the prescribedDate to the current time.
     */
    @PrePersist // Specifies a callback method for the corresponding lifecycle event.
    protected void onCreate() {
        prescribedDate = LocalDateTime.now(); // Sets the timestamp to the current system time.
    }
}
