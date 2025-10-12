package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByKeycloakId(UUID keycloakId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    @EntityGraph(attributePaths = "accounts")
    Optional<User> findWithAccountsById(UUID id);
}