package com.example.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
        UUID id,
        UUID keycloakId,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        Instant createdAt,
        Instant updatedAt
) {}