package com.malcolm.medicaliot.model;

import jakarta.persistence.*;

/**
 * Represents a User in the Medical IoT System.
 * This entity stores login credentials, roles, and personal details for all
 * system actors
 * (Doctors, Nurses, Patients, Admins).
 */
@Entity
@Table(name = "users") // Maps to the "users" table in the database
public class User {

    /**
     * Unique identifier for the user.
     * Auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for login. Must be unique.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Encrypted password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role of the user in the system.
     * Expected values: "DOCTOR", "NURSE", "PATIENT", "ADMIN".
     * Used for authorization and access control.
     */
    @Column(nullable = false)
    private String role;

    /**
     * Department the user belongs to (e.g., "CARDIOLOGY", "GENERAL").
     * Relevant for Doctors and Nurses.
     */
    private String department;

    /**
     * Additional attributes for Attribute-Based Encryption (ABE) or flexible data
     * storage.
     * Can be stored as a JSON string or comma-separated list.
     */
    private String attributes;

    // --- Profile Information ---

    /**
     * Full name of the user for display purposes.
     */
    private String fullName;

    private int age;

    private String gender; // M, F, Other

    // Default constructor required by JPA
    public User() {
    }

    /**
     * Parameterized constructor for creating user instances.
     */
    public User(Long id, String username, String password, String role, String department, String attributes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.attributes = attributes;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
