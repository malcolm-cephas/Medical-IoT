package com.malcolm.medicalauth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    private String department;
    private String attributes;

    private String fullName;
    private int age;
    private String gender;
    private String address;

    private String specialization;
    private String clearanceLevel;

    private String referredBy;
    private String reasonOfAdmission;
    private LocalDateTime dateOfAdmission;
    
    private String wardName;
    private Integer wardNumber;

    @Column(columnDefinition = "TEXT")
    private String faceDescriptor;

    public User() {
    }

    public User(Long id, String username, String password, String role, String department, String attributes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.attributes = attributes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getClearanceLevel() { return clearanceLevel; }
    public void setClearanceLevel(String clearanceLevel) { this.clearanceLevel = clearanceLevel; }
    public String getReferredBy() { return referredBy; }
    public void setReferredBy(String referredBy) { this.referredBy = referredBy; }
    public String getReasonOfAdmission() { return reasonOfAdmission; }
    public void setReasonOfAdmission(String reasonOfAdmission) { this.reasonOfAdmission = reasonOfAdmission; }
    public LocalDateTime getDateOfAdmission() { return dateOfAdmission; }
    public void setDateOfAdmission(LocalDateTime dateOfAdmission) { this.dateOfAdmission = dateOfAdmission; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public Integer getWardNumber() { return wardNumber; }
    public void setWardNumber(Integer wardNumber) { this.wardNumber = wardNumber; }

    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }
}
