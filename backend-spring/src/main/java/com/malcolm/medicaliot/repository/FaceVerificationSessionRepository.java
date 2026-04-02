package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.FaceVerificationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FaceVerificationSessionRepository extends JpaRepository<FaceVerificationSession, Long> {
    Optional<FaceVerificationSession> findTopByUsernameAndActiveTrueOrderByVerifiedAtDesc(String username);
}
