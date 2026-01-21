package com.malcolm.medicaliot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // DOCTOR, NURSE, PATIENT, ADMIN

    private String department; // CARDIOLOGY, GENERAL, etc.

    // We can store additional attributes as a JSON string or comma-separated list
    // for ABE
    private String attributes;
}
