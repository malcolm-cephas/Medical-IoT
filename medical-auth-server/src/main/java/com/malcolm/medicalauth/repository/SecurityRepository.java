package com.malcolm.medicalauth.repository;

import com.malcolm.medicalauth.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityRepository extends JpaRepository<SecurityEvent, Long> {
}
