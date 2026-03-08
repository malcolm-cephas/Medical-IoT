package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByRoleAndUsernameContainingIgnoreCase(String role, String username, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchByRoleAndName(@org.springframework.data.repository.query.Param("role") String role, @org.springframework.data.repository.query.Param("search") String search, Pageable pageable);
}
