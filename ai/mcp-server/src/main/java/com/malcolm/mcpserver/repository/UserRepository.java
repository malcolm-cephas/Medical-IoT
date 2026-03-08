package com.malcolm.mcpserver.repository;

import com.malcolm.mcpserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(String role);

    Optional<User> findByUsername(String username);
}
